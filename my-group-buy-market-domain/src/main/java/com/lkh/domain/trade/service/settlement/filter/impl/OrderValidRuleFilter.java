package com.lkh.domain.trade.service.settlement.filter.impl;

import com.lkh.domain.trade.adapter.repository.ITradeRepository;
import com.lkh.domain.trade.model.entity.GroupBuyOrderEntity;
import com.lkh.domain.trade.model.entity.GroupBuyOrderListEntity;
import com.lkh.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.lkh.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.lkh.domain.trade.service.settlement.factory.TradeSettlementOrderRuleFilterFactory;
import com.lkh.domain.trade.service.settlement.filter.AbstractTradeSettlementOrderRuleFilter;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * 校验订单是否超时
 */
@Component
public class OrderValidRuleFilter extends AbstractTradeSettlementOrderRuleFilter {

    @Autowired
    private ITradeRepository tradeRepository;
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementOrderRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        String outTradeNo = requestParameter.getOutTradeNo();
        GroupBuyOrderListEntity groupBuyOrderListEntity = dynamicContext.getGroupBuyOrderListEntity();
        String teamId = groupBuyOrderListEntity.getTeamId();
        if(Objects.isNull(outTradeNo)){
            throw new AppException(ResponseCode.NOT_NULL);
        }

        // 获取拼团信息
        GroupBuyOrderEntity groupBuyOrderEntity = tradeRepository.queryGroupBuyOrderEntity(teamId);

        Date validEndTime = groupBuyOrderEntity.getValidEndTime();
        Date outTradeTime = requestParameter.getOutTradeTime();
        // 查看下单时间是否在拼团结束时间之后
        if(validEndTime.before(outTradeTime)){
            throw new AppException(ResponseCode.E0116);
        }

        // 添加拼团信息到上下文
        dynamicContext.setGroupBuyOrderEntity(groupBuyOrderEntity);
        return next(requestParameter, dynamicContext);
    }
}
