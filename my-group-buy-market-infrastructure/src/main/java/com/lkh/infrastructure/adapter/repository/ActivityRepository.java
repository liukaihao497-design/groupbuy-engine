package com.lkh.infrastructure.adapter.repository;
import java.math.BigDecimal;
import java.util.Date;

import com.lkh.domain.activity.adapter.repository.IActivityRepository;
import com.lkh.domain.activity.model.entity.GroupBuyTeamEntity;
import com.lkh.domain.activity.model.entity.GroupBuyTeamUserEntity;
import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.UserGroupBuyTeamEntity;
import com.lkh.domain.activity.model.valobj.*;
import com.lkh.infrastructure.dao.*;
import com.lkh.infrastructure.dao.po.*;
import com.lkh.infrastructure.dcc.DCCService;
import com.lkh.infrastructure.redis.IRedisService;
import com.lkh.infrastructure.utils.mapper.ObjMapper;
import com.lkh.types.enums.GroupBuyOrderStatus;
import io.jsonwebtoken.lang.Collections;
import org.redisson.api.RBitSet;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ActivityRepository extends AbstractRepository implements IActivityRepository {
    @Autowired
    private GroupBuyActivityMapper activityMapper;
    @Autowired
    private GroupBuyDiscountMapper discountMapper;
    @Autowired
    private ScGoodsActivityMapper scGoodsActivityMapper;
    @Autowired
    private IRedisService redisService;
    @Resource
    private DCCService dccService;
    @Autowired
    private CrowdTagsDetailMapper crowdTagsDetailMapper;
    @Autowired
    private ObjMapper objMapper;
    @Autowired
    private GroupBuyOrderMapper groupBuyOrderMapper;
    @Autowired
    private GroupBuyOrderListMapper groupBuyOrderListMapper;
    @Override
    public List<GroupBuyActivityDiscountVO> queryActivityDiscounts(String goodsId, String source, String channel) {

        GroupBuyActivityDiscountVO activityDiscountVO = queryActivityDiscounts(goodsId, source, channel, null);
        return new ArrayList() {{
            add(activityDiscountVO);
        }};
    }

    private GroupBuyActivityDiscountVO queryActivityDiscounts(String goodsId, String source, String channel, String activityId) {
        GroupBuyActivity groupBuyActivity;
        if (activityId != null) {
            String key = GroupBuyActivity.cacheRedisKey(activityId);
            String finalActivityId = activityId;
            groupBuyActivity = getFromCacheOrDb(key,()->{
                return activityMapper.queryActivityById(finalActivityId);
            });
        } else {
            // 查询指定渠道商品的活动
            String scKey = ScGoodsActivity.cacheRedisKey(source,channel,goodsId);
            activityId = getFromCacheOrDb(scKey,()->{
                return scGoodsActivityMapper.queryActivityId(source,channel,goodsId);
            });
            String key = GroupBuyActivity.cacheRedisKey(activityId);
            groupBuyActivity = getFromCacheOrDb(key,()->{
                return scGoodsActivityMapper.queryGroupBuyActivity(goodsId, source, channel);
            });
        }
        if (groupBuyActivity == null) {
            return null;
        }
        // 查询具体折扣信息
        GroupBuyDiscount discount = getFromCacheOrDb(GroupBuyDiscount.cacheRedisKey(groupBuyActivity.getDiscountId()),()->{
            return discountMapper.queryByDiscountId(groupBuyActivity.getDiscountId());
        });

        if (discount == null) {
            discount = new GroupBuyDiscount();
        }
        // 组装 并 填充VO
        GroupBuyActivityDiscountVO VO = new GroupBuyActivityDiscountVO();
        GroupBuyActivityDiscountVO.GroupBuyDiscount discountVO = new GroupBuyActivityDiscountVO.GroupBuyDiscount();
        BeanUtils.copyProperties(groupBuyActivity, VO);
        BeanUtils.copyProperties(discount, discountVO);

        VO.setSource(source);
        VO.setChannel(channel);
        VO.setGoodsId(goodsId);
        //设置活动 折扣配置的枚举类
        discountVO.setDiscountType(DiscountTypeEnum.get(discount.getDiscountType()));

        VO.setGroupBuyDiscount(discountVO);
        return VO;
    }

    @Override
    public TagIdScopeVO queryTagIdAndScopeByActivityId(Long activityId) {
        GroupBuyActivity groupBuyActivity = activityMapper.queryActivityById(String.valueOf(activityId));
        TagIdScopeVO vo = new TagIdScopeVO();
        vo.setTagId(groupBuyActivity.getTagId());
        vo.setTagScope(groupBuyActivity.getTagScope());
        return vo;
    }

    @Override
    public boolean isTagIdUser(String activityTagId, String userId) {
        RBitSet bitSet = redisService.getBitSet(activityTagId);
        int offset = redisService.getIndexFromUserId(userId);
        boolean b = bitSet.get(offset);

        return b;
    }

    @Override
    public boolean isCutRange(String userId) {
        boolean b = dccService.isCutRange(userId);
        return b;
    }

    @Override
    public boolean isDownGradeSwitch() {
        boolean b = dccService.isDowngradeSwitch();
        return b;
    }

    @Override
    public GroupBuyActivityDiscountVO queryActivityDiscounts(String activityId) {
        ScGoodsActivity sc = scGoodsActivityMapper.querySCByActivityId(activityId);
        return queryActivityDiscounts(sc.getGoodsId(), sc.getSource(), sc.getChannel(),null);
    }

    @Override
    public boolean isCrowdTagMatch(String userId, String discountTag) {
        List<String>userIds = crowdTagsDetailMapper.queryUserIdByTagId(discountTag);
        return userIds.contains(userId);
    }

    @Override
    public String queryActivityIdBySCGoodsId(String source, String channel, String goodsId) {
        ScGoodsActivity req = new ScGoodsActivity();
        req.setSource(source);
        req.setChannel(channel);
        req.setGoodsId(goodsId);
        ScGoodsActivity scGoodsActivity = scGoodsActivityMapper.query(req);
        if(Objects.isNull(scGoodsActivity) || Objects.isNull(scGoodsActivity.getActivityId())) {
            return null;
        }

        return String.valueOf(scGoodsActivity.getActivityId());
    }

    @Override
    public List<GroupBuyTeamEntity> queryGroupBuyTeamEntity(String source, String channel, String activityId,Integer teamLimit) {

        GroupBuyOrder req = new GroupBuyOrder();
        req.setSource(source);
        req.setChannel(channel);
        req.setActivityId(Long.parseLong(activityId));
        req.setStatus(GroupBuyOrderStatus.create.getCode());
        List<GroupBuyOrder> list = groupBuyOrderMapper.queryList(req);
        if(Collections.isEmpty(list)){
            return new ArrayList<>();
        }
        List<GroupBuyTeamEntity> resultList = objMapper.groupBuyOrderList2GroupBuyTeamEntityList(list);

        if(resultList.size() <= teamLimit){
            return resultList;
        }
        // 队伍下用户数量超过limit，排除超出数量后返回
        for (int i = 0; i < (resultList.size() - teamLimit); i++) {
            resultList.remove(resultList.size() - 1 - i);
        }
        return resultList;

    }

    @Override
    public List<GroupBuyTeamUserEntity> queryUserTeamInfo(String teamId,Integer userTeamLimit) {
        GroupBuyOrderList req = new GroupBuyOrderList();
        req.setTeamId(teamId);
        List<GroupBuyOrderList> list = groupBuyOrderListMapper.queryList(req);
        if(Collections.isEmpty(list)){
            return new ArrayList<>();
        }
        List<GroupBuyTeamUserEntity> resultList = objMapper.groupBuyOrderListList2GroupBuyTeamUserEntityList(list);
        if(resultList.size() <= userTeamLimit){
            return resultList;
        }
        // 队伍下用户数量超过limit，排除超出数量后返回
        for (int i = 0; i < (resultList.size() - userTeamLimit); i++) {
            resultList.remove(resultList.size() - 1 - i);
        }
        return resultList;
    }

    @Override
    public ActivityTeamStatistic statisticTeam(String source, String channel,String goodsId,String activityId) {
        int allTeamCount = groupBuyOrderMapper.countAllTeamNumber(source,channel,activityId);
        int allTeamCompleteCount = groupBuyOrderMapper.countAllCompleteTeamNumber(source,channel,activityId);
        int allTeamUserCount = groupBuyOrderListMapper.countAllTeamUserCount(goodsId);
        ActivityTeamStatistic activityTeamStatistic = new ActivityTeamStatistic(allTeamCount, allTeamCompleteCount, allTeamUserCount);

        return activityTeamStatistic;
    }

    @Override
    public UserGroupBuyTeamEntity queryUserTeamInfo(String userId, String source, String channel, String goodsId) {
        // 查询当前来源渠道下商品的活动id
        ScGoodsActivity req = new ScGoodsActivity();
        req.setSource(source);
        req.setChannel(channel);
        req.setGoodsId(goodsId);
        ScGoodsActivity res = scGoodsActivityMapper.query(req);
        if (Objects.isNull(res)) {
            return null;
        }
        Long activityId = res.getActivityId();

        // 按当前页面的完整业务范围查询用户最近一条有效参团记录。
        GroupBuyOrderList queryUserTeamListReq = new GroupBuyOrderList();
        queryUserTeamListReq.setActivityId(activityId);
        queryUserTeamListReq.setUserId(userId);
        queryUserTeamListReq.setGoodsId(goodsId);
        queryUserTeamListReq.setSource(source);
        queryUserTeamListReq.setChannel(channel);
        GroupBuyOrderList userTeamListReq =
                groupBuyOrderListMapper.queryLatestValidUserTeam(queryUserTeamListReq);

        // 如果为空 说明当前用户没有在当前活动商品下开团 直接返回null
        if(Objects.isNull(userTeamListReq)){
            return null;
        }

        // 根据用户拼团的teamid 查询拼团信息
        String userTeamId = userTeamListReq.getTeamId();
        GroupBuyOrder groupBuyOrderReq = new GroupBuyOrder();
        groupBuyOrderReq.setTeamId(userTeamId);
        groupBuyOrderReq.setActivityId(activityId);
        groupBuyOrderReq.setSource(source);
        groupBuyOrderReq.setChannel(channel);
        groupBuyOrderReq.setStatus(GroupBuyOrderStatus.create.getCode());
        GroupBuyOrder groupBuyOrderRes = groupBuyOrderMapper.query(groupBuyOrderReq);
        if (Objects.isNull(groupBuyOrderRes)
                || Objects.isNull(groupBuyOrderRes.getValidEndTime())
                || !groupBuyOrderRes.getValidEndTime().after(new Date())
                || groupBuyOrderRes.getLockCount() >= groupBuyOrderRes.getTargetCount()) {
            return null;
        }
        String teamId = groupBuyOrderRes.getTeamId();
        Integer targetCount = groupBuyOrderRes.getTargetCount();
        Integer completeCount = groupBuyOrderRes.getCompleteCount();
        Integer lockCount = groupBuyOrderRes.getLockCount();
        Date validStartTime = groupBuyOrderRes.getValidStartTime();
        Date validEndTime = groupBuyOrderRes.getValidEndTime();

        String outTradeNo = userTeamListReq.getOutTradeNo();

        // 组装返回结果
        UserGroupBuyTeamEntity userGroupBuyTeamEntity = new UserGroupBuyTeamEntity();
        userGroupBuyTeamEntity.setUserId(userId);
        userGroupBuyTeamEntity.setTeamId(teamId);
        userGroupBuyTeamEntity.setActivityId(activityId);
        userGroupBuyTeamEntity.setTargetCount(targetCount);
        userGroupBuyTeamEntity.setCompleteCount(completeCount);
        userGroupBuyTeamEntity.setLockCount(lockCount);
        userGroupBuyTeamEntity.setValidStartTime(validStartTime);
        userGroupBuyTeamEntity.setValidEndTime(validEndTime);
        userGroupBuyTeamEntity.setOutTradeNo(outTradeNo);
        userGroupBuyTeamEntity.setOrderStatus(userTeamListReq.getStatus());

        return userGroupBuyTeamEntity;
    }

    @Override
    public List<UserGroupBuyTeamEntity> queryRandomTeamInfo(RandomTeamInfoQuery randomTeamInfoQuery) {
        String source = randomTeamInfoQuery.getSource();
        String channel = randomTeamInfoQuery.getChannel();
        Long activityId = randomTeamInfoQuery.getActivityId();
        String goodsId = randomTeamInfoQuery.getGoodsId();
        Integer teamLimit = randomTeamInfoQuery.getTeamLimit();
        String teamId = randomTeamInfoQuery.getTeamId();

        // 指定查询队伍信息时的数量限制
        int count = teamLimit << 1;
        GroupBuyOrder groupBuyOrderReq  = new GroupBuyOrder();
        groupBuyOrderReq.setSource(source);
        groupBuyOrderReq.setChannel(channel);
        groupBuyOrderReq.setActivityId(activityId);
        groupBuyOrderReq.setTeamId(teamId);
        // 查询指定数量的拼团队伍
        List<GroupBuyOrder> groupBuyOrderList =
                groupBuyOrderMapper.queryListWithLimit(groupBuyOrderReq, goodsId, count);

        // 如果为空 则直接返回空集合
        if(Collections.isEmpty(groupBuyOrderList)){
            return java.util.Collections.emptyList();
        }

        // 如果拼团队伍超出限制，打乱后截取指定数量的队伍
        if(groupBuyOrderList.size() > teamLimit){
            java.util.Collections.shuffle(groupBuyOrderList);
            groupBuyOrderList = groupBuyOrderList.subList(0,teamLimit);
        }

        // 查询每个队伍的最新参团人员信息
        List<String> teamIds = groupBuyOrderList.stream().map(GroupBuyOrder::getTeamId).collect(Collectors.toList());
        List<GroupBuyOrderList> list = groupBuyOrderListMapper.queryUserOrderInfo(teamIds);
        Map<String, GroupBuyOrderList> map = list.stream().collect(Collectors.toMap(GroupBuyOrderList::getTeamId, e -> e));

        // 组装返回集合
        List<UserGroupBuyTeamEntity> resultList = new ArrayList<>(groupBuyOrderList.size());
        for (GroupBuyOrder groupBuyOrder : groupBuyOrderList) {
            String curTeamId = groupBuyOrder.getTeamId();
            Long curActivity = groupBuyOrder.getActivityId();
            Integer targetCount = groupBuyOrder.getTargetCount();
            Integer completeCount = groupBuyOrder.getCompleteCount();
            Integer lockCount = groupBuyOrder.getLockCount();
            Date validStartTime = groupBuyOrder.getValidStartTime();
            Date validEndTime = groupBuyOrder.getValidEndTime();

            GroupBuyOrderList userOrderInfo = map.get(curTeamId);
            if(userOrderInfo == null){
                continue;
            }
            String userId = userOrderInfo.getUserId();
            UserGroupBuyTeamEntity userGroupBuyTeamEntity = new UserGroupBuyTeamEntity();
            userGroupBuyTeamEntity.setUserId(userId);
            userGroupBuyTeamEntity.setTeamId(curTeamId);
            userGroupBuyTeamEntity.setActivityId(curActivity);
            userGroupBuyTeamEntity.setTargetCount(targetCount);
            userGroupBuyTeamEntity.setCompleteCount(completeCount);
            userGroupBuyTeamEntity.setLockCount(lockCount);
            userGroupBuyTeamEntity.setValidStartTime(validStartTime);
            userGroupBuyTeamEntity.setValidEndTime(validEndTime);
            // 随机展示用户只用于头像/昵称展示，不向前端返回其交易单号。
            userGroupBuyTeamEntity.setOutTradeNo(null);
            userGroupBuyTeamEntity.setOrderStatus(null);

            resultList.add(userGroupBuyTeamEntity);
        }
        return resultList;
    }


}
