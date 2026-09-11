package com.lkh.domain.activity.service.trial;

import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.service.trial.factory.DefaultTrialFactory;
import com.lkh.types.design.framework.AbstractMultiThreadStategyRouter;
import com.lkh.types.design.framework.AbstractStrategyRouter;

public abstract class AbstractGroupBuyMarketSupport extends AbstractMultiThreadStategyRouter<MarketProductEntity, DefaultTrialFactory.DynamicContext, TrialBalanceEntity> {

}
