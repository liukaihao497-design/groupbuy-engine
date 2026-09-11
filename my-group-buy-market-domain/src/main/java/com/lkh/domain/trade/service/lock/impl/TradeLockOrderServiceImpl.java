package com.lkh.domain.trade.service.lock.impl;

import com.lkh.domain.trade.adapter.repository.ITradeRepository;
import com.lkh.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.lkh.domain.trade.model.entity.*;
import com.lkh.domain.trade.model.valobj.GroupBuyProgressVO;
import com.lkh.domain.trade.service.ITradeLockOrderService;
import com.lkh.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import com.lkh.types.design.link.model2.chain.BusinessLinkedList;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class TradeLockOrderServiceImpl implements ITradeLockOrderService {
    @Autowired
    private BusinessLinkedList<TradeRuleCommandEntity,TradeRuleFilterFactory.DynamicContext,TradeRuleFilterBackEntity> tradeRuleFilter;
    @Autowired
    private ITradeRepository tradeRepository;

    @Override
    public GroupBuyOrderListEntity queryNoPayOrder(String userId, String outTradeId) {
        GroupBuyOrderListEntity entity = tradeRepository.queryNoPayOrder(userId, outTradeId);

        return entity;
    }

    @Override
    public boolean createUserGroupBuyOrder() {
        return false;
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProcess(String teamId) {

        GroupBuyProgressVO vo = tradeRepository.queryGroupBuyProcess(teamId);
        return vo;
    }


    @Override
    public MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity) {
        // 参数校验
        log.info("拼团交易-锁定营销优惠支付订单:{} activityId:{} goodsId:{}", userEntity.getUserId(), payActivityEntity.getActivityId(), payDiscountEntity.getGoodsId());
        // 交易规则过滤
        TradeRuleFilterBackEntity tradeRuleFilterBackEntity = null;
        try {
            tradeRuleFilterBackEntity = tradeRuleFilter.apply(TradeRuleCommandEntity.builder()
                            .activityId(payActivityEntity.getActivityId())
                            .userId(userEntity.getUserId())
                            .teamId(payActivityEntity.getTeamId())
                            .build(),
                    new TradeRuleFilterFactory.DynamicContext());
        } catch (AppException e) {
            throw e;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 已参与拼团量 - 用于构建数据库唯一索引使用，确保用户只能在一个活动上参与固定的次数
        Integer userTakeOrderCount = tradeRuleFilterBackEntity.getUserTakeOrderCount();


        // 构建聚合对象
        GroupBuyOrderAggregate aggreate = GroupBuyOrderAggregate.builder()
                .userEntity(userEntity)
                .payActivityEntity(payActivityEntity)
                .userTakeOrderCount(userTakeOrderCount)
                .payDiscountEntity(payDiscountEntity).build();

        try {
            // 锁定聚合订单 - 这会用户只是下单还没有支付。后续会有2个流程；支付成功、超时未支付（回退）
            return tradeRepository.lockMarketPayOrder(aggreate);
        } catch (Exception e) {
            // 记录失败恢复量
            tradeRepository.recoveryTeamStock(tradeRuleFilterBackEntity.getRecoveryTeamStockKey(), payActivityEntity.getValidTime());
            throw e;
        }
    }


}
