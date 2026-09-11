package com.lkh.domain.trade.service.settlement.filter.impl;
import com.lkh.domain.trade.model.entity.GroupBuyOrderEntity;
import com.lkh.domain.trade.model.entity.GroupBuyOrderListEntity;
import com.lkh.types.enums.GroupBuyOrderStatus;
import java.util.Date;

import com.lkh.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.lkh.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.lkh.domain.trade.service.settlement.factory.TradeSettlementOrderRuleFilterFactory;
import com.lkh.domain.trade.service.settlement.filter.AbstractTradeSettlementOrderRuleFilter;
import org.springframework.stereotype.Component;

/**
 * 结束节点，负责组装返回数据
 */
@Component
public class EndRuleFilter extends AbstractTradeSettlementOrderRuleFilter {
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementOrderRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        GroupBuyOrderListEntity groupBuyOrderListEntity = dynamicContext.getGroupBuyOrderListEntity();
        GroupBuyOrderEntity groupBuyOrderEntity = dynamicContext.getGroupBuyOrderEntity();
        Integer targetCount = groupBuyOrderEntity.getTargetCount();
        Integer completeCount = groupBuyOrderEntity.getCompleteCount();
        Integer lockCount = groupBuyOrderEntity.getLockCount();
        String notifyUrl = groupBuyOrderEntity.getNotifyUrl();
        String teamId = groupBuyOrderListEntity.getTeamId();
        Long activityId = groupBuyOrderListEntity.getActivityId();


        TradeSettlementRuleFilterBackEntity result = new TradeSettlementRuleFilterBackEntity();
        result.setTeamId(teamId);
        result.setActivityId(activityId);
        result.setTargetCount(targetCount);
        result.setCompleteCount(completeCount);
        result.setLockCount(lockCount);
        result.setStatus(GroupBuyOrderStatus.create);
        result.setValidStartTime(new Date());
        result.setValidEndTime(new Date());
        result.setGroupBuyOrderList(groupBuyOrderListEntity);
        result.setNotifyUrl(notifyUrl);
        result.setNotifyConfigVO(groupBuyOrderEntity.getNotifyConfigVO());
        return result;
    }
}
