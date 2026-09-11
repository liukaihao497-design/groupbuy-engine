package com.lkh.domain.trade.service.settlement.filter.impl;

import com.lkh.domain.trade.adapter.repository.ITradeRepository;
import com.lkh.domain.trade.model.entity.GroupBuyOrderListEntity;
import com.lkh.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.lkh.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.lkh.domain.trade.service.settlement.factory.TradeSettlementOrderRuleFilterFactory;
import com.lkh.domain.trade.service.settlement.filter.AbstractTradeSettlementOrderRuleFilter;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OutTradeNoRuleFilter extends AbstractTradeSettlementOrderRuleFilter {
    @Autowired
    private ITradeRepository tradeRepository;
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementOrderRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        String outTradeNo = requestParameter.getOutTradeNo();
        if(StringUtils.isEmpty(outTradeNo)){
            throw new AppException(ResponseCode.NOT_NULL);
        }
        // 根据交易单号，查询用户拼团记录
        GroupBuyOrderListEntity groupBuyOrderListEntity = tradeRepository.queryGroupBuyOrderListEntityByOutTradeNo(outTradeNo);
        if(Objects.isNull(groupBuyOrderListEntity)){
            // 记录为空，单号不存在
            throw new AppException(ResponseCode.E0110);
        }
        dynamicContext.setGroupBuyOrderListEntity(groupBuyOrderListEntity);
        return next(requestParameter, dynamicContext);
    }
}
