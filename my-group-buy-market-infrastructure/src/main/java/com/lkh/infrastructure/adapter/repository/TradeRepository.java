package com.lkh.infrastructure.adapter.repository;

import java.math.BigDecimal;

import com.alibaba.fastjson.JSON;
import com.lkh.domain.trade.adapter.repository.ITradeRepository;
import com.lkh.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.lkh.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.lkh.domain.trade.model.entity.*;
import com.lkh.domain.trade.model.valobj.GroupBuyProgressVO;
import com.lkh.domain.trade.model.valobj.NotifyConfigVO;
import com.lkh.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import com.lkh.domain.trade.model.valobj.enums.NotifyTypeEnumVO;
import com.lkh.infrastructure.dao.GroupBuyOrderListMapper;
import com.lkh.infrastructure.dao.GroupBuyOrderMapper;
import com.lkh.infrastructure.dao.NotifyTaskMapper;
import com.lkh.infrastructure.dao.po.GroupBuyOrder;
import com.lkh.infrastructure.dao.po.GroupBuyOrderList;
import com.lkh.infrastructure.dao.po.NotifyTask;
import com.lkh.infrastructure.dcc.DCCService;
import com.lkh.infrastructure.redis.IRedisService;
import com.lkh.infrastructure.utils.mapper.ObjMapper;
import com.lkh.types.common.Constants;
import com.lkh.types.enums.GroupBuyOrderListStatus;
import com.lkh.types.enums.GroupBuyOrderStatus;
import com.lkh.types.enums.NotifyTaskStatus;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class TradeRepository implements ITradeRepository {
    @Autowired
    private NotifyTaskMapper notifyTaskMapper;
    @Autowired
    private ObjMapper objMapper;
    @Autowired
    private GroupBuyOrderListMapper groupBuyOrderListMapper;
    @Autowired
    private GroupBuyOrderMapper groupBuyOrderMapper;

    @Autowired
    private DCCService dccService;

    @Autowired
    private IRedisService redisService;

    @Value("${spring.rabbitmq.config.producer.topic_team_success.routing_key}")
    private String topicTeamSuccessRoutingKey;
    @Override
    public GroupBuyOrderListEntity queryNoPayOrder(String userId, String outTradeId) {
        GroupBuyOrderList order = groupBuyOrderListMapper.queryNoPayOrder(userId, outTradeId);
        GroupBuyOrderListEntity entity = objMapper.po2Entity(order);
        return entity;
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProcess(String teamId) {
        GroupBuyOrder req = new GroupBuyOrder();
        req.setTeamId(teamId);
        GroupBuyOrder res = groupBuyOrderMapper.query(req);
        GroupBuyProgressVO vo = objMapper.po2VO(res);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate aggreate) {


        if (aggreate == null) {
            throw new RuntimeException("锁单聚合参数不能为空");
        }

        UserEntity userEntity = aggreate.getUserEntity();
        PayActivityEntity payActivityEntity = aggreate.getPayActivityEntity();
        PayDiscountEntity payDiscountEntity = aggreate.getPayDiscountEntity();
        BigDecimal originalPrice = payDiscountEntity.getOriginalPrice();
        NotifyConfigVO notifyConfigVO = payActivityEntity.getNotifyConfigVO();
        if (notifyConfigVO == null || notifyConfigVO.getNotifyType() == null) {
            throw new AppException(ResponseCode.NOT_NULL);
        }
        if (NotifyTypeEnumVO.HTTP.equals(notifyConfigVO.getNotifyType())
                && StringUtils.isBlank(notifyConfigVO.getNotifyUrl())) {
            throw new AppException(ResponseCode.NOT_NULL);
        }
        BigDecimal payPrice = payDiscountEntity.getOriginalPrice().subtract(payDiscountEntity.getDeductionPrice());

        String teamId = payActivityEntity.getTeamId();
        if (StringUtils.isBlank(teamId)) {
            teamId = RandomStringUtils.randomNumeric(8);

            // 构建拼团订单
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder()
                    .teamId(teamId)
                    .activityId(payActivityEntity.getActivityId())
                    .source(payDiscountEntity.getSource())
                    .channel(payDiscountEntity.getChannel())
                    .originalPrice(payDiscountEntity.getOriginalPrice())
                    .deductionPrice(payDiscountEntity.getDeductionPrice())
                    .payPrice(payPrice)
                    .targetCount(payActivityEntity.getTargetCount())
                    .completeCount(0)
                    .lockCount(1)
                    .status(0)
                    .validStartTime(new Date())
                    .validEndTime(new Date(new Date().getTime() + 60 * 60 * 1000 * 12))
                    .notifyUrl(notifyConfigVO.getNotifyUrl())
                    .notifyType(notifyConfigVO.getNotifyType().getCode())
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            groupBuyOrderMapper.insert(groupBuyOrder);
        }else{
            // 存在拼团队伍 修改人数
            int i = groupBuyOrderMapper.incyOneCount(teamId);
            if(i != 1) throw new AppException(ResponseCode.E0006);
        }
        // 加入拼团
        String orderId = RandomStringUtils.randomNumeric(12);
        GroupBuyOrderList groupBuyOrderList = GroupBuyOrderList.builder()
                .userId(userEntity.getUserId())
                .teamId(teamId)
                .orderId(orderId)
                .activityId(payActivityEntity.getActivityId())
                .startTime(payActivityEntity.getStartTime())
                .endTime(payActivityEntity.getEndTime())
                .goodsId(payDiscountEntity.getGoodsId())
                .source(payDiscountEntity.getSource())
                .channel(payDiscountEntity.getChannel())
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payPrice)
                .status(TradeOrderStatusEnumVO.CREATE.getCode())
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                // 构建 bizId 唯一值；活动id_用户id_参与次数累加
                .bizId(payActivityEntity.getActivityId() + "_" + userEntity.getUserId() + "_" + (aggreate.getUserTakeOrderCount() + 1))

                .createTime(new Date())
                .updateTime(new Date())
                .build();
        try {
            groupBuyOrderListMapper.insert(groupBuyOrderList);
        }catch (DuplicateKeyException e){
            throw new AppException(ResponseCode.INDEX_EXCEPTION);
        }

        MarketPayOrderEntity marketPayOrderEntity = MarketPayOrderEntity.builder()
                .teamId(teamId)
                .orderId(orderId)
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payPrice)
                .originalPrice(originalPrice)
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE)
                .build();
        return marketPayOrderEntity;

    }

    @Override
    public Integer queryOrderCountByActivityId(Long activityId, String userId) {
        Integer orderCount = groupBuyOrderListMapper.queryUserValidOrderCount(userId,activityId);
        return orderCount;
    }

    @Override
    public GroupBuyOrderListEntity queryGroupBuyOrderListEntityByOutTradeNo(String outTradeNo) {
        GroupBuyOrderList req = new GroupBuyOrderList();
        req.setOutTradeNo(outTradeNo);
        GroupBuyOrderList groupBuyOrderList = groupBuyOrderListMapper.query(req);
        GroupBuyOrderListEntity entity = objMapper.toEntity(groupBuyOrderList);
        return entity;
    }

    @Override
    public int setSuccessPayOrderStatus(String userId, String orderId) {
        int i = groupBuyOrderListMapper.setSuccessPayOrderStatus(userId,orderId, GroupBuyOrderListStatus.payed.getCode(),GroupBuyOrderStatus.create.getCode());

        return i;
    }
    public int setSuccessPayOrderStatusAndOutTradeTime(String userId, String orderId,Date outTradeTime) {
        int i = groupBuyOrderListMapper.setSuccessPayOrderStatusAndOutTradeTime(userId,orderId,outTradeTime, GroupBuyOrderListStatus.payed.getCode(),GroupBuyOrderStatus.create.getCode());

        return i;
    }

    @Override
    public int setSuccessGroupBuyOrderStatus(String teamId) {
        if(StringUtils.isBlank(teamId)){
            throw new RuntimeException("teamId不能为空");
        }
        return groupBuyOrderMapper.setSuccessGroupBuyOrderStatus(teamId,GroupBuyOrderStatus.complete.getCode());
    }

    @Override
    public int incryGroupBuyOrderCompleteCount(String teamId) {
        int i = groupBuyOrderMapper.incyCompleteCount(teamId);
        return i;
    }

//    @Override
    private void addNotifyTask(String teamId , String activityId,String notifyUrl) {
        if(StringUtils.isBlank(teamId) || StringUtils.isBlank(activityId)||StringUtils.isBlank(notifyUrl)){
            throw new AppException(ResponseCode.NOT_NULL);
        }
        // 查询此活动下的拼团队伍成员
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListMapper.queryListByTeamIdAndActivityId(teamId,activityId);
        List<String> outTradeNoList = groupBuyOrderLists.stream().map(GroupBuyOrderList::getOutTradeNo)
                .collect(Collectors.toList());

        HashMap<String, Object> map = new HashMap<>();
        map.put("teamId",teamId);
        map.put("outTradeNoList",outTradeNoList);


        // 构建回调任务
        NotifyTask req = new NotifyTask();
        req.setActivityId(Long.valueOf(activityId));
        req.setTeamId(teamId);
        req.setNotifyUrl("暂无");
        req.setNotifyCount(0);
        req.setNotifyUrl(notifyUrl);
        req.setNotifyStatus(NotifyTaskStatus.create.getCode());
        req.setParameterJson(JSON.toJSONString(map));
        req.setCreateTime(new Date());
        req.setUpdateTime(new Date());
        int i = notifyTaskMapper.insert(req);
        if(i != 1){
            throw new AppException(ResponseCode.E0114);
        }

    }

    /**
     * 根据通知配置创建回调任务。MQ 路由键由服务端统一配置，避免由调用方任意指定。
     */
    private NotifyTaskEntity addNotifyTask(String teamId, String activityId, NotifyConfigVO notifyConfigVO) {
        if (StringUtils.isBlank(teamId) || StringUtils.isBlank(activityId)
                || notifyConfigVO == null || notifyConfigVO.getNotifyType() == null) {
            throw new AppException(ResponseCode.NOT_NULL);
        }
        if (NotifyTypeEnumVO.HTTP.equals(notifyConfigVO.getNotifyType())
                && StringUtils.isBlank(notifyConfigVO.getNotifyUrl())) {
            throw new AppException(ResponseCode.NOT_NULL);
        }

        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListMapper.queryListByTeamIdAndActivityId(teamId, activityId);
        List<String> outTradeNoList = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getOutTradeNo)
                .collect(Collectors.toList());

        HashMap<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("teamId", teamId);
        parameterMap.put("outTradeNoList", outTradeNoList);

        NotifyTask notifyTask = new NotifyTask();
        notifyTask.setActivityId(Long.valueOf(activityId));
        notifyTask.setTeamId(teamId);
        notifyTask.setNotifyType(notifyConfigVO.getNotifyType().getCode());
        notifyTask.setNotifyMq(NotifyTypeEnumVO.MQ.equals(notifyConfigVO.getNotifyType())
                ? topicTeamSuccessRoutingKey : null);
        notifyTask.setNotifyUrl(NotifyTypeEnumVO.HTTP.equals(notifyConfigVO.getNotifyType())
                ? notifyConfigVO.getNotifyUrl() : null);
        notifyTask.setNotifyCount(0);
        notifyTask.setNotifyStatus(NotifyTaskStatus.create.getCode());
        notifyTask.setParameterJson(JSON.toJSONString(parameterMap));
        notifyTask.setCreateTime(new Date());
        notifyTask.setUpdateTime(new Date());

        int insertCount = notifyTaskMapper.insert(notifyTask);
        if (insertCount != 1) {
            throw new AppException(ResponseCode.E0114);
        }

        return NotifyTaskEntity.builder()
                .id(notifyTask.getId())
                .activityId(notifyTask.getActivityId())
                .teamId(notifyTask.getTeamId())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMq())
                .notifyUrl(notifyTask.getNotifyUrl())
                .notifyCount(notifyTask.getNotifyCount())
                .notifyStatus(NotifyTaskStatus.valueOf(notifyTask.getNotifyStatus()))
                .parameterJson(notifyTask.getParameterJson())
                .build();
    }

    @Override
    @Transactional
    public void settlementMarketPayOrder(GroupBuyTeamSettlementAggregate requestParam) {
        settlementMarketPayOrderAndCreateNotifyTask(requestParam);
    }

    @Override
    @Transactional
    public NotifyTaskEntity settlementMarketPayOrderAndCreateNotifyTask(GroupBuyTeamSettlementAggregate requestParam) {
        GroupBuyOrderListEntity groupBuyTeamEntity = requestParam.getGroupBuyTeamEntity();
        TradePaySuccessEntity tradePaySuccessEntity = requestParam.getTradePaySuccessEntity();
        NotifyConfigVO notifyConfigVO = requestParam.getNotifyConfigVO();
        UserEntity userEntity = requestParam.getUserEntity();
        String userId = userEntity.getUserId();
        String teamId = groupBuyTeamEntity.getTeamId();
        String orderId = groupBuyTeamEntity.getOrderId();
        Long activityId = groupBuyTeamEntity.getActivityId();
        String notifyUrl = tradePaySuccessEntity.getNotifyUrl();
        if (notifyConfigVO == null && StringUtils.isNotBlank(notifyUrl)) {
            // 兼容旧的 HTTP 结算调用。
            notifyConfigVO = NotifyConfigVO.builder()
                    .notifyType(NotifyTypeEnumVO.HTTP)
                    .notifyUrl(notifyUrl)
                    .build();
        }
        Date outTradeTime = tradePaySuccessEntity.getOutTradeTime();
        // 更新用户拼单记录订单状态
        int i = this.setSuccessPayOrderStatusAndOutTradeTime(userId,orderId,outTradeTime);
        if(i <= 0){
            GroupBuyOrderList req = new GroupBuyOrderList();
            req.setOrderId(orderId);
            GroupBuyOrderList res = groupBuyOrderListMapper.query(req);
            Integer orderStatus = res.getStatus();
            if(GroupBuyOrderListStatus.payed.getCode().equals(orderStatus)){
                return null;
            }
            throw new AppException(ResponseCode.E0112);
        }
        // 修改拼团完成数量
        int j = this.incryGroupBuyOrderCompleteCount(teamId);
        if(j <= 0){
            throw new AppException(ResponseCode.E0113);
        }
       /* GroupBuyOrder groupBuyOrder = new GroupBuyOrder();
        groupBuyOrder.setTeamId(teamId);
        GroupBuyOrder teamOrder = groupBuyOrderMapper.query(groupBuyOrder);
        // 校验完成数量 是否完成拼团
        if(Objects.equals(teamOrder.getTargetCount(),teamOrder.getCompleteCount())){
            // 修改拼团队伍状态
            this.setSuccessGroupBuyOrderStatus(teamId);
            // 添加回调任务
            this.addNotifyTask(teamId,String.valueOf(activityId));
        }*/
        int isSuccess = groupBuyOrderMapper.setSuccessIfCompleteTarget(teamId,GroupBuyOrderStatus.complete.getCode());
        if(isSuccess == 1){
            // 添加回调任务
            return this.addNotifyTask(teamId, String.valueOf(activityId), notifyConfigVO);
        }
        return null;
    }

    @Override
    public GroupBuyOrderEntity queryGroupBuyOrderEntity(String teamId) {
        GroupBuyOrder req = new GroupBuyOrder();
        req.setTeamId(teamId);
        GroupBuyOrder res = groupBuyOrderMapper.query(req);
        GroupBuyOrderEntity entity = objMapper.toEntity(res);
        if (entity == null) {
            return null;
        }
        NotifyTypeEnumVO notifyType = StringUtils.isBlank(res.getNotifyType())
                ? NotifyTypeEnumVO.HTTP
                : NotifyTypeEnumVO.valueOf(res.getNotifyType());
        entity.setNotifyConfigVO(NotifyConfigVO.builder()
                .notifyType(notifyType)
                .notifyMQ(NotifyTypeEnumVO.MQ.equals(notifyType) ? topicTeamSuccessRoutingKey : null)
                .notifyUrl(res.getNotifyUrl())
                .build());
        return entity;
    }

    @Override
    public boolean isSCBlackList(String source, String channel) {
        return  dccService.isScBlacklist(source,channel);
    }

    /**
     * 占用库存
     * <p>
     * 关于 Redis 独占锁和无锁化设计；<a href="https://bugstack.cn/md/road-map/redis.html">Redis 缓存、加锁(独占/分段)、发布/订阅，常用特性的使用和高级编码操作</a>
     */
    @Override
    public boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime) {
        // 失败恢复量
        Long recoveryCount = redisService.getAtomicLong(recoveryTeamStockKey);
        recoveryCount = null == recoveryCount ? 0 : recoveryCount;

        // 1. incr 得到值，与总量和恢复量做对比。恢复量为系统失败时候记录的量。
        // 2. 从有组队量开始，相当于已经有了一个占用量，所以要 +1
        long occupy = redisService.incr(teamStockKey) + 1;

        if (occupy > target + recoveryCount) {
            redisService.setAtomicLong(teamStockKey, target);
            return false;
        }

        // 1. 给每个产生的值加锁为兜底设计，虽然incr操作是原子的，基本不会产生一样的值。但在实际生产中，遇到过集群的运维配置问题，以及业务运营配置数据问题，导致incr得到的值相同。
        // 2. validTime + 60分钟，是一个延后时间的设计，让数据保留时间稍微长一些，便于排查问题。
        String lockKey = teamStockKey + Constants.UNDERLINE + occupy;
        Boolean lock = redisService.setNx(lockKey, validTime + 60, TimeUnit.MINUTES);

        if (!lock) {
            log.info("组队库存加锁失败 {}", lockKey);
        }

        return lock;
    }

    @Override
    public void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime) {
        // 首次组队拼团，是没有 teamId 的，所以不需要这个做处理。
        if (StringUtils.isBlank(recoveryTeamStockKey)) return;

        redisService.incr(recoveryTeamStockKey);
    }
}
