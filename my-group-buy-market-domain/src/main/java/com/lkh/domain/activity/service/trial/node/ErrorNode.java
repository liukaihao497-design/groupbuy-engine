package com.lkh.domain.activity.service.trial.node;

import com.alibaba.fastjson.JSON;
import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.lkh.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.lkh.domain.activity.service.trial.factory.DefaultTrialFactory;
import com.lkh.types.design.framework.StrategyHandler;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ErrorNode extends AbstractGroupBuyMarketSupport {
    @Override
    protected void multiThreadLoadContext(MarketProductEntity requestparamj, DefaultTrialFactory.DynamicContext context) throws Exception {

    }

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) throws Exception {
        log.info("拼团商品查询试算服务-NoMarketNode userId:{} requestParameter:{}", requestparam.getUserId(), JSON.toJSONString(requestparam));
        List<GroupBuyActivityDiscountVO> vos = context.getDiscountVOList();
        if(Collections.isEmpty(vos) || vos.get(0).getGroupBuyDiscount() == null){
           throw new AppException(ResponseCode.E0002.getCode(), ResponseCode.E0002.getInfo());
        }

        return TrialBalanceEntity.builder().build();
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultTrialFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) throws Exception {
        return null;
    }
}
