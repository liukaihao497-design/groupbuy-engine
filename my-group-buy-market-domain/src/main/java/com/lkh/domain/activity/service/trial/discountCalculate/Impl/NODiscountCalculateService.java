package com.lkh.domain.activity.service.trial.discountCalculate.Impl;

import com.lkh.domain.activity.service.trial.discountCalculate.AbstractDiscountCalculateService;
import com.lkh.domain.activity.service.trial.discountCalculate.DiscountCalculateService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("NO")
public class NODiscountCalculateService extends AbstractDiscountCalculateService {
    @Override
    protected BigDecimal doCalculateDiscountPrice(BigDecimal originalPrice, String marketExpr) {

        return calculateDiscountValue(originalPrice, marketExpr);
    }

    @Override
    public BigDecimal calculateDiscountValue(BigDecimal originalPrice, String marketExpr) {
        return originalPrice;
    }
}
