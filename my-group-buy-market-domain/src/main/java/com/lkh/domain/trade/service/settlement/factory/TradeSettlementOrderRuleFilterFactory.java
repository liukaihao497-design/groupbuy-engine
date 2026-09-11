package com.lkh.domain.trade.service.settlement.factory;

import com.lkh.domain.trade.model.entity.GroupBuyOrderEntity;
import com.lkh.domain.trade.model.entity.GroupBuyOrderListEntity;
import com.lkh.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.lkh.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.lkh.domain.trade.service.settlement.filter.impl.EndRuleFilter;
import com.lkh.domain.trade.service.settlement.filter.impl.OrderValidRuleFilter;
import com.lkh.domain.trade.service.settlement.filter.impl.OutTradeNoRuleFilter;
import com.lkh.domain.trade.service.settlement.filter.impl.ScRuleFilter;
import com.lkh.types.design.link.model2.LinkArmory;
import com.lkh.types.design.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradeSettlementOrderRuleFilterFactory {

    @Bean("tradeSettlementRuleFilter")
    public BusinessLinkedList<TradeSettlementRuleCommandEntity, TradeSettlementOrderRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity>
    tradeSettlementRuleFilter(ScRuleFilter scRuleFilter,
                              OutTradeNoRuleFilter outTradeNoRuleFilter,
                              OrderValidRuleFilter orderValidRuleFilter,
                              EndRuleFilter endRuleFilter) {
        LinkArmory<TradeSettlementRuleCommandEntity, DynamicContext, TradeSettlementRuleFilterBackEntity>
                list = new LinkArmory<>("交易结算规则过滤链", scRuleFilter, outTradeNoRuleFilter, orderValidRuleFilter, endRuleFilter);
        return list.getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        GroupBuyOrderListEntity groupBuyOrderListEntity;
        GroupBuyOrderEntity groupBuyOrderEntity;
    }
}
