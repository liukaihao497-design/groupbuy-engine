package com.lkh.domain.activity.service.trial.node;

import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.lkh.domain.activity.service.trial.factory.DefaultTrialFactory;
import com.lkh.types.design.framework.StrategyHandler;
import com.lkh.types.exception.ParamNullException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Slf4j
@Service
public class RootNode extends AbstractGroupBuyMarketSupport {
    @Resource
    private SwitchNode switchNode;


    @Override
    public StrategyHandler<MarketProductEntity, DefaultTrialFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) {
        return switchNode;
    }

    @Override
    protected void multiThreadLoadContext(MarketProductEntity requestparamj, DefaultTrialFactory.DynamicContext context) {

    }

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) throws Exception{
        // 参数校验
        if(StringUtils.isBlank(requestparam.getChannel())||StringUtils.isBlank(requestparam.getGoodsId())||
        StringUtils.isBlank(requestparam.getSource())||StringUtils.isBlank(requestparam.getUserId())
        ) throw new ParamNullException();

        return router(requestparam, context);
    }
}
