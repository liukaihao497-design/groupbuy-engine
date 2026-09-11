package com.lkh.domain.trade.service;

import com.lkh.domain.trade.model.entity.NotifyTaskEntity;
import com.lkh.domain.trade.model.entity.TradePaySettlementEntity;
import com.lkh.domain.trade.model.entity.TradePaySuccessEntity;

import java.util.Map;

public interface ITradeSettlementOrderService {

//    TradePaySettlementEntity settlementMarketPayOrderBak(TradePaySuccessEntity tradePaySuccessEntity) ;


    TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity);


    /**
     * 执行结算通知任务
     *
     * @return 结算数量
     * @throws Exception 异常
     */
    Map<String, Integer> execSettlementNotifyJob() throws Exception;

    /**
     * 执行结算通知任务
     *
     * @param teamId 指定结算组ID
     * @return 结算数量
     * @throws Exception 异常
     */
    Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception;

    /**
     * 执行指定的结算通知任务，用于结算成功后的即时通知。
     */
    Map<String, Integer> execSettlementNotifyJob(NotifyTaskEntity notifyTaskEntity) throws Exception;
}
