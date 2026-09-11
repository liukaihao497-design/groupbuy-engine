package com.lkh.domain.trade.service.settlement.filter.impl;

import com.lkh.domain.trade.adapter.repository.ITradeRepository;
import com.lkh.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.lkh.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.lkh.domain.trade.service.settlement.factory.TradeSettlementOrderRuleFilterFactory;
import com.lkh.domain.trade.service.settlement.filter.AbstractTradeSettlementOrderRuleFilter;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScRuleFilter extends AbstractTradeSettlementOrderRuleFilter {
    @Autowired
    private ITradeRepository tradeRepository;
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementOrderRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        String source = requestParameter.getSource();
        String channel = requestParameter.getChannel();
        boolean b = tradeRepository.isSCBlackList(source,channel);
        if(b){
            throw new AppException(ResponseCode.E0117);
        }

        return next(requestParameter, dynamicContext);
    }
}
