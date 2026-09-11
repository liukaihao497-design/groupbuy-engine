package com.lkh.domain.trade.service.settlement.impl;
import java.time.LocalDateTime;
import java.util.*;

import com.lkh.domain.trade.adapter.port.INotifyTaskPort;
import com.lkh.domain.trade.adapter.repository.ITradeRepository;
import com.lkh.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.lkh.domain.trade.model.entity.*;
import com.lkh.domain.trade.model.valobj.GroupBuyProgressVO;
import com.lkh.domain.trade.model.valobj.NotifyConfigVO;
import com.lkh.domain.trade.service.ITradeLockOrderService;
import com.lkh.domain.trade.service.ITradeSettlementOrderService;
import com.lkh.domain.trade.service.settlement.factory.TradeSettlementOrderRuleFilterFactory;
import com.lkh.types.design.link.model2.handler.ILogicHandler;
import com.lkh.types.enums.NotifyTaskStatus;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;


@Slf4j
@Service

public class TradeSettlementOrderService implements ITradeSettlementOrderService {
    @Resource
    private ITradeRepository tradeRepository;

    @Resource
    private ITradeLockOrderService tradeLockOrderService;

    @Resource
    private INotifyTaskPort notifyTaskPort;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementOrderRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter;
    @Override
    public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) {
        String source = tradePaySuccessEntity.getSource();
        String channel = tradePaySuccessEntity.getChannel();
        String userId = tradePaySuccessEntity.getUserId();
        String outTradeNo = tradePaySuccessEntity.getOutTradeNo();
        Date outTradeTime = new Date();

        // 构建责任链请求参数，进行校验
        TradeSettlementRuleCommandEntity requestParam = new TradeSettlementRuleCommandEntity();
        requestParam.setSource(source);
        requestParam.setChannel(channel);
        requestParam.setUserId(userId);
        requestParam.setOutTradeNo(outTradeNo);
        requestParam.setOutTradeTime(outTradeTime);
        TradeSettlementRuleFilterBackEntity filterRes;
        try {
             filterRes =
                    tradeSettlementRuleFilter.apply(requestParam, new TradeSettlementOrderRuleFilterFactory.DynamicContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


       /* // 查询用户的下单记录
        GroupBuyOrderListEntity groupBuyOrderListEntity = tradeRepository.queryGroupBuyOrderListEntityByOutTradeNo(outTradeNo);
        // 校验单号
        if (groupBuyOrderListEntity == null) {
            throw new AppException(ResponseCode.E0110);
        }
        // 校验单号从属用户和传入用户是否相同
        if (!Objects.equals(groupBuyOrderListEntity.getUserId(), userId)) {
            throw new AppException(ResponseCode.E0111);
        }
        String teamId = groupBuyOrderListEntity.getTeamId();
*/
        GroupBuyOrderListEntity groupBuyOrderListEntity = filterRes.getGroupBuyOrderList();
        String teamId = filterRes.getTeamId();
        String notifyUrl = filterRes.getNotifyUrl();
        NotifyConfigVO notifyConfigVO = filterRes.getNotifyConfigVO();
        // 构建订单结算聚合参数
        GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate = new GroupBuyTeamSettlementAggregate();
        groupBuyTeamSettlementAggregate.setUserEntity(UserEntity.builder().userId(userId).build());
        groupBuyTeamSettlementAggregate.setGroupBuyTeamEntity(groupBuyOrderListEntity);
        groupBuyTeamSettlementAggregate.setTradePaySuccessEntity(TradePaySuccessEntity.builder().source(source).channel(channel).userId(userId).outTradeNo(outTradeNo).notifyUrl(notifyUrl).outTradeTime(outTradeTime).build());
        groupBuyTeamSettlementAggregate.setNotifyConfigVO(notifyConfigVO);

        NotifyTaskEntity notifyTaskEntity = tradeRepository.settlementMarketPayOrderAndCreateNotifyTask(groupBuyTeamSettlementAggregate);

        // 即时通知只负责提高时效；发送失败或线程池执行异常时，落库任务仍会由定时任务补偿。
        if (notifyTaskEntity != null) {
            threadPoolExecutor.execute(() -> {
                try {
                    Map<String, Integer> result = execSettlementNotifyJob(notifyTaskEntity);
                    log.info("拼团结算即时通知执行完成 teamId:{} result:{}", notifyTaskEntity.getTeamId(), result);
                } catch (Exception e) {
                    log.error("拼团结算即时通知执行失败，等待定时任务补偿 teamId:{}", notifyTaskEntity.getTeamId(), e);
                }
            });
        }










        // 构建返回值
        TradePaySettlementEntity res = new TradePaySettlementEntity();
        res.setChannel(channel);
        res.setOutTradeNo(outTradeNo);
        res.setSource(source);
        res.setTeamId(teamId);
        res.setActivityId(groupBuyOrderListEntity.getActivityId());
        res.setUserId(userId);
        return res;

    }

    /*   @Override
       public TradePaySettlementEntity settlementMarketPayOrderBak(TradePaySuccessEntity tradePaySuccessEntity) {
           String source = tradePaySuccessEntity.getSource();
           String channel = tradePaySuccessEntity.getChannel();
           String userId = tradePaySuccessEntity.getUserId();
           String outTradeNo = tradePaySuccessEntity.getOutTradeNo();

           // 查询用户的下单记录
           GroupBuyOrderListEntity groupBuyOrderListEntity = tradeRepository.queryGroupBuyOrderListEntityByOutTradeNo(outTradeNo);
           // 校验单号
           if (groupBuyOrderListEntity == null) {
               throw new AppException(ResponseCode.E0110);
           }
           // 校验单号从属用户和传入用户是否相同
           if (!Objects.equals(groupBuyOrderListEntity.getUserId(), userId)) {
               throw new AppException(ResponseCode.E0111);
           }
           String teamId = groupBuyOrderListEntity.getTeamId();

           // 校验拼团是否结束？？？？


           // 更新拼单记录订单状态
           int i = tradeRepository.setSuccessPayOrderStatus(userId,groupBuyOrderListEntity.getOrderId());
           if(i <= 0){
               throw new AppException(ResponseCode.E0112);
           }
           // 修改拼团完成数量
           int j = tradeRepository.incryGroupBuyOrderCompleteCount(teamId);
           if(j <= 0){
               throw new AppException(ResponseCode.E0113);
           }
           // 校验完成数量 是否完成拼团
           GroupBuyProgressVO groupBuyProgressVO = tradeLockOrderService.queryGroupBuyProcess(teamId);
           if(Objects.equals(groupBuyProgressVO.getTargetCount(),groupBuyProgressVO.getCompleteCount())){
               // 完成拼团
               handlerGroupBuyActivityComplete(teamId,String.valueOf(groupBuyOrderListEntity.getActivityId()));
           }

           // 构建返回值
           TradePaySettlementEntity res = new TradePaySettlementEntity();
           res.setChannel(channel);
           res.setOutTradeNo(outTradeNo);
           res.setSource(source);
           res.setTeamId(teamId);
           res.setActivityId(groupBuyOrderListEntity.getActivityId());
           res.setUserId(userId);
           return res;
       }

       private void handlerGroupBuyActivityComplete(String teamId,String activityId) {
           // 修改拼团队伍状态
           tradeRepository.setSuccessGroupBuyOrderStatus(teamId);
           // 添加回调任务
           tradeRepository.addNotifyTask(teamId,activityId);
       }*/
    @Override
    public Map<String, Integer> execSettlementNotifyJob() throws Exception {
        List<NotifyTaskEntity> list = notifyTaskPort.queryNoCompleteORRetryTask();
        Map<String, Integer> map = execSettlementNotifyJob(list);
        return map;
    }

    @Override
    public Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception {
        List<NotifyTaskEntity> list = notifyTaskPort.queryTaskByTeamId(teamId);
        Map<String, Integer> map = execSettlementNotifyJob(list);
        return map;
    }

    @Override
    public Map<String, Integer> execSettlementNotifyJob(NotifyTaskEntity notifyTaskEntity) throws Exception {
        if (notifyTaskEntity == null) {
            return execSettlementNotifyJob(Collections.emptyList());
        }
        return execSettlementNotifyJob(Collections.singletonList(notifyTaskEntity));
    }


    private Map<String, Integer> execSettlementNotifyJob(List<NotifyTaskEntity> notifyTaskEntityList) throws Exception {
        if (notifyTaskEntityList == null) {
            notifyTaskEntityList = Collections.emptyList();
        }
        int successCount = 0, errorCount = 0, retryCount = 0;
        for (NotifyTaskEntity notifyTaskEntity : notifyTaskEntityList) {
            NotifyTaskStatus notifyStatus = notifyTaskEntity.getNotifyStatus();
           /* if(Objects.equals(notifyStatus,NotifyTaskStatus.fail)){
                // 通知失败任务
                errorCount++;
                continue;
            }
            if(Objects.equals(notifyStatus,NotifyTaskStatus.complete)){
                successCount++;
                // 任务已经被处理
                continue;
            }*/
            //处理任务
            NotifyTaskStatus status = notifyTaskPort.execSettlementNotify(notifyTaskEntity);
            if(status == null){
                continue;
            }
            if(Objects.equals(status,NotifyTaskStatus.complete)){
                // 完成任务
                int i = notifyTaskPort.setSuccessAndIncryCount(notifyTaskEntity.getTeamId());
                if(i == 1){
                    successCount++;
                }
            }else if (Objects.equals(status,NotifyTaskStatus.retry)){
                Integer notifyCount = Optional.ofNullable(notifyTaskEntity.getNotifyCount()).orElse(0);
                if(notifyCount < 5){
                    int i = notifyTaskPort.setRetryAndIncryCount(notifyTaskEntity.getTeamId());
                    if(i == 1){
                        retryCount++;
                    }
                }else{
                    int i = notifyTaskPort.setFailAndIncryCount(notifyTaskEntity.getTeamId());
                    if(i == 1){
                        errorCount++;
                    }
                }
            }
            else if(Objects.equals(status,NotifyTaskStatus.fail)){
                int i = notifyTaskPort.setFailAndIncryCount(notifyTaskEntity.getTeamId());
                if(i == 1){
                    errorCount++;
                }
            }

        }
        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("waitCount", notifyTaskEntityList.size());
        resultMap.put("successCount", successCount);
        resultMap.put("errorCount", errorCount);
        resultMap.put("retryCount", retryCount);
    return resultMap;
    }

    }
