package com.lkh.domain.trade.service.settlement.filter;

import com.lkh.domain.trade.model.entity.*;
import com.lkh.domain.trade.service.settlement.factory.TradeSettlementOrderRuleFilterFactory;
import com.lkh.types.design.link.model2.chain.BusinessLinkedList;
import com.lkh.types.design.link.model2.handler.ILogicHandler;

import org.springframework.stereotype.Component;


@Component
public abstract class AbstractTradeSettlementOrderRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementOrderRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {


}
