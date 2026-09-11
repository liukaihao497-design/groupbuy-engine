package com.lkh.domain.trade.service.lock.filter;

import com.lkh.domain.trade.model.entity.TradeRuleCommandEntity;
import com.lkh.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.lkh.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import com.lkh.types.design.link.model2.handler.ILogicHandler;

public abstract class AbstractRulerFilter implements ILogicHandler<TradeRuleCommandEntity,TradeRuleFilterFactory.DynamicContext,TradeRuleFilterBackEntity> {

}
