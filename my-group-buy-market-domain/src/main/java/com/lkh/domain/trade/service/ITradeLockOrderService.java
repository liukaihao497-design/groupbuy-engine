package com.lkh.domain.trade.service;

import com.lkh.domain.trade.model.entity.*;
import com.lkh.domain.trade.model.valobj.GroupBuyProgressVO;

public interface ITradeLockOrderService {
    // 查询指定流水号的用户拼团单
    GroupBuyOrderListEntity queryNoPayOrder(String userId,String outTradeId);
    // 创建用户拼团单记录
    boolean createUserGroupBuyOrder ();

    /**
     * 查询拼团进度
     * @param teamId 拼团id
     * @return
     */
    GroupBuyProgressVO queryGroupBuyProcess(String teamId);


    /**
     * 锁定，营销预支付订单；商品下单前，预购锁定。
     *
     * @param userEntity        用户根实体对象
     * @param payActivityEntity 拼团，支付活动实体对象
     * @param payDiscountEntity 拼团，支付优惠实体对象
     * @return 拼团，预购订单营销实体对象
     */
    MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity);

}
