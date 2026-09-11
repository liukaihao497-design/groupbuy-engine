package com.lkh.domain.trade.adapter.repository;

import com.lkh.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.lkh.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.lkh.domain.trade.model.entity.GroupBuyOrderEntity;
import com.lkh.domain.trade.model.entity.GroupBuyOrderListEntity;
import com.lkh.domain.trade.model.entity.MarketPayOrderEntity;
import com.lkh.domain.trade.model.entity.NotifyTaskEntity;
import com.lkh.domain.trade.model.valobj.GroupBuyProgressVO;

public interface ITradeRepository {
    /**
     * 查询状态为0 即初始锁定但未完成付款的订单
     * @param userId 用户id
     * @param outTradeId 外部交易订单号
     * @return
     */
    GroupBuyOrderListEntity queryNoPayOrder(String userId,String outTradeId);

    GroupBuyProgressVO queryGroupBuyProcess(String teamId);

    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate aggreate);

    Integer queryOrderCountByActivityId(Long activityId, String userId);

    GroupBuyOrderListEntity queryGroupBuyOrderListEntityByOutTradeNo(String outTradeNo);

    int setSuccessPayOrderStatus(String userId, String orderId);

    int setSuccessGroupBuyOrderStatus(String teamId);

    int incryGroupBuyOrderCompleteCount(String teamId);

//    void addNotifyTask(String teamId , String activityId);

    /**
     * 结算用户拼团订单
     */
    void settlementMarketPayOrder(GroupBuyTeamSettlementAggregate requestParam);

    /**
     * 结算用户拼团订单，并在拼团达成时返回同事务创建的通知任务。
     * 原 settlementMarketPayOrder 方法保留用于兼容已有调用。
     */
    NotifyTaskEntity settlementMarketPayOrderAndCreateNotifyTask(GroupBuyTeamSettlementAggregate requestParam);

    GroupBuyOrderEntity queryGroupBuyOrderEntity(String teamId);

    boolean isSCBlackList(String source, String channel);

    boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime);

    void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime);
}
