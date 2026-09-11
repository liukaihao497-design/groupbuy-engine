package com.lkh.domain.trade.service.lock.factory;

import com.lkh.domain.trade.model.entity.GroupBuyActivityEntity;
import com.lkh.domain.trade.model.entity.TradeRuleCommandEntity;
import com.lkh.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.lkh.domain.trade.service.lock.filter.impl.ActivityUsabilityRuleFilter;
import com.lkh.domain.trade.service.lock.filter.impl.TeamStockOccupyRuleFilter;
import com.lkh.domain.trade.service.lock.filter.impl.UserTakeLimitRuleFilter;
import com.lkh.types.design.link.model2.LinkArmory;
import com.lkh.types.design.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradeRuleFilterFactory {

    @Bean("tradeRuleFilter")
    public BusinessLinkedList<TradeRuleCommandEntity, DynamicContext, TradeRuleFilterBackEntity>
    tradeRuleFilter(ActivityUsabilityRuleFilter activityUsabilityRuleFilter,
                    UserTakeLimitRuleFilter userTakeLimitRuleFilter,
                    TeamStockOccupyRuleFilter teamStockOccupyRuleFilter) {
        // 组装链
        LinkArmory<TradeRuleCommandEntity, TradeRuleFilterFactory.DynamicContext, TradeRuleFilterBackEntity> linkArmory =
                new LinkArmory<>("交易规则过滤链", activityUsabilityRuleFilter, userTakeLimitRuleFilter,teamStockOccupyRuleFilter);

        // 链对象
        return linkArmory.getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private String teamStockKey = "group_buy_market_team_stock_key_";


        private GroupBuyActivityEntity groupBuyActivity;
        // 用户参与活动的订单量
        private Integer userTakeOrderCount;

        public String generateTeamStockKey(String teamId) {
            if (StringUtils.isBlank(teamId)) return null;
            return teamStockKey + groupBuyActivity.getActivityId() + "_" + teamId;
        }

        public String generateRecoveryTeamStockKey(String teamId) {
            if (StringUtils.isBlank(teamId)) return null;
            return teamStockKey + groupBuyActivity.getActivityId() + "_" + teamId + "_recovery";
        }



    }

}