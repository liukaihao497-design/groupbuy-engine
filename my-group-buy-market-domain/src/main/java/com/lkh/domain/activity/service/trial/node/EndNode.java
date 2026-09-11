package com.lkh.domain.activity.service.trial.node;

import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.lkh.domain.activity.service.trial.factory.DefaultTrialFactory;
import com.lkh.types.design.framework.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EndNode extends AbstractGroupBuyMarketSupport {



    @Override
    public StrategyHandler<MarketProductEntity, DefaultTrialFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) {
        return null;
    }

    @Override
    protected void multiThreadLoadContext(MarketProductEntity requestparamj, DefaultTrialFactory.DynamicContext context) {
    }

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) throws Exception{
        return TrialBalanceEntity.builder()
                .goodsId(requestparam.getGoodsId())
                .goodsName(context.getSkuVOS().get(0).getGoodsName())
                .originalPrice(context.getSkuVOS().get(0).getOriginalPrice())
                .deductionPrice(context.getDeductionPrice())
                .payPrice(context.getPayPrice())
                .targetCount(context.getDiscountVOList().get(0).getTarget())
                .startTime(context.getDiscountVOList().get(0).getStartTime())
                .endTime(context.getDiscountVOList().get(0).getEndTime())
                .activityId(context.getDiscountVOList().get(0).getActivityId())
                .isVisible(context.isVisible())
                .isEnable(context.isEnable())
                .build();
    }
}
