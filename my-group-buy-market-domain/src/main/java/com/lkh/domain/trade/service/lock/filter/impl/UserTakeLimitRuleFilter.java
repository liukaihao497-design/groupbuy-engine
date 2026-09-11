package com.lkh.domain.trade.service.lock.filter.impl;


import com.lkh.domain.trade.adapter.repository.ITradeRepository;
import com.lkh.domain.trade.model.entity.GroupBuyActivityEntity;
import com.lkh.domain.trade.model.entity.TradeRuleCommandEntity;
import com.lkh.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.lkh.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import com.lkh.domain.trade.service.lock.filter.AbstractRulerFilter;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 用户参与限制，规则过滤
 * @create 2025-01-25 09:19
 */
@Slf4j
@Service
public class UserTakeLimitRuleFilter extends AbstractRulerFilter {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeRuleFilterBackEntity apply(TradeRuleCommandEntity requestParameter, TradeRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("交易规则过滤-用户参与次数校验{} activityId:{}", requestParameter.getUserId(), requestParameter.getActivityId());

        GroupBuyActivityEntity groupBuyActivity = dynamicContext.getGroupBuyActivity();

        // 查询用户在一个拼团活动上参与的次数
        Integer count = repository.queryOrderCountByActivityId(requestParameter.getActivityId(), requestParameter.getUserId());

        if (null != groupBuyActivity.getTakeLimitCount() && count >= groupBuyActivity.getTakeLimitCount()) {
            log.info("用户参与次数校验，已达可参与上限 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0103);

        }

        dynamicContext.setUserTakeOrderCount(count);
        return next(requestParameter, dynamicContext);
    }

}
