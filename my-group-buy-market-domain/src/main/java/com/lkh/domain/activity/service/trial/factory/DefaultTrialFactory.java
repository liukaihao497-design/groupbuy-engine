package com.lkh.domain.activity.service.trial.factory;

import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.lkh.domain.activity.model.valobj.SkuVO;
import com.lkh.domain.activity.service.trial.node.RootNode;
import com.lkh.types.design.framework.StrategyHandler;
import lombok.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DefaultTrialFactory {

    private final RootNode rootNode;

    public DefaultTrialFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }
    public StrategyHandler<MarketProductEntity, DynamicContext, TrialBalanceEntity> getTrialHandler() {
        return rootNode;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DynamicContext {
        private List<SkuVO> skuVOS;
        List<GroupBuyActivityDiscountVO> discountVOList;

        BigDecimal deductionPrice;

        BigDecimal payPrice;
        // 判断非活动标签用户是否可见可参与
        boolean visible;
        boolean enable;
    }
}
