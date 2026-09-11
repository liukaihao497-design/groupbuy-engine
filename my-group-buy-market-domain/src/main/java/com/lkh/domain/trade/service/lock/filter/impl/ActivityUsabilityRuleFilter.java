package com.lkh.domain.trade.service.lock.filter.impl;

import com.lkh.domain.trade.adapter.port.IActivityPort;
import com.lkh.domain.trade.model.entity.GroupBuyActivityEntity;
import com.lkh.domain.trade.model.entity.TradeRuleCommandEntity;
import com.lkh.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.lkh.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import com.lkh.domain.trade.service.lock.filter.AbstractRulerFilter;
import com.lkh.types.enums.ActivityStatusEnumVO;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class ActivityUsabilityRuleFilter extends AbstractRulerFilter {

    @Autowired
    private IActivityPort activityPort;

    @Override
    public TradeRuleFilterBackEntity apply(TradeRuleCommandEntity requestParameter, TradeRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        GroupBuyActivityEntity groupBuyActivity = activityPort.queryGroupBuyActivityEntityByActivityId(requestParameter.getActivityId());
        // 校验；活动状态 - 可以抛业务异常code，或者把code写入到动态上下文dynamicContext中，最后获取。
        if (!ActivityStatusEnumVO.EFFECTIVE.equals(groupBuyActivity.getStatus())) {
            log.info("活动的可用性校验，非生效状态 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0101);
        }

        // 校验；活动时间
        Date currentTime = new Date();
        if (currentTime.before(groupBuyActivity.getStartTime()) || currentTime.after(groupBuyActivity.getEndTime())) {
            log.info("活动的可用性校验，非可参与时间范围 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0102);
        }

        // 写入动态上下文
        dynamicContext.setGroupBuyActivity(groupBuyActivity);

        // 走到下一个责任链节点
        return next(requestParameter, dynamicContext);

    }
}
