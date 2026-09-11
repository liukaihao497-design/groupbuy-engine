package com.lkh.domain.activity.service.trial.discountCalculate.Impl;

import com.lkh.domain.activity.service.trial.discountCalculate.AbstractDiscountCalculateService;
import com.lkh.domain.activity.service.trial.discountCalculate.DiscountCalculateService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("N")
public class NDiscountCalculateServiceImpl extends AbstractDiscountCalculateService {
    @Override
    public BigDecimal doCalculateDiscountPrice(BigDecimal originalPrice, String marketExpr) {
        //参数校验

        //计算折扣
        BigDecimal discountValue = calculateDiscountValue(originalPrice, marketExpr);
        BigDecimal resultPrice = originalPrice.subtract(discountValue);
        return money(resultPrice);
    }

    @Override
    public BigDecimal calculateDiscountValue(BigDecimal originalPrice, String marketExpr) {
        BigDecimal discountValue = originalPrice.subtract(new BigDecimal(marketExpr));
        return discountValue.compareTo(BigDecimal.ZERO) >= 0 ? discountValue : originalPrice;
    }
}
