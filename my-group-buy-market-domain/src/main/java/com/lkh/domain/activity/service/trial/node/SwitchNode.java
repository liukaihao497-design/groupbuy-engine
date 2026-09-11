package com.lkh.domain.activity.service.trial.node;

import com.lkh.domain.activity.adapter.repository.IActivityRepository;
import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.lkh.domain.activity.service.trial.factory.DefaultTrialFactory;
import com.lkh.types.design.framework.StrategyHandler;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.annotation.Resources;

@Slf4j
@Service
public class SwitchNode extends AbstractGroupBuyMarketSupport {

    @Resource
    private MarketTrialNode marketTrialNode;
    @Resource
    private IActivityRepository activityRepository;

    @Override
    public StrategyHandler<MarketProductEntity, DefaultTrialFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) {
        return marketTrialNode;
    }

    @Override
    protected void multiThreadLoadContext(MarketProductEntity requestparamj, DefaultTrialFactory.DynamicContext context) {

    }

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) throws Exception{
        // 判断是否做降级处理
        if(activityRepository.isDownGradeSwitch()){

            throw new AppException(ResponseCode.E0003.getCode(),ResponseCode.E0003.getInfo());
        }
        // 是否做切量处理
        if(!activityRepository.isCutRange(requestparam.getUserId())){
            throw new AppException(ResponseCode.E0004.getCode(),ResponseCode.E0004.getInfo());
        }

        return router(requestparam, context);
    }
}
