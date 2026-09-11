package com.lkh.domain.activity.service.trial.discountCalculate.Impl;

import com.lkh.domain.activity.service.trial.discountCalculate.AbstractDiscountCalculateService;
import com.lkh.domain.activity.service.trial.discountCalculate.DiscountCalculateService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("ZK")
public class ZKDiscountCalculateServiceImpl extends AbstractDiscountCalculateService {
    @Override
    public BigDecimal doCalculateDiscountPrice(BigDecimal originalPrice, String marketExpr) {
        //参数校验

        //计算优惠金额
        BigDecimal discountValue = calculateDiscountValue(originalPrice, marketExpr);
        BigDecimal resultPrice = originalPrice.subtract(discountValue);

        return money(resultPrice);
    }

    @Override
    public BigDecimal calculateDiscountValue(BigDecimal originalPrice, String marketExpr) {
        BigDecimal discountValue = originalPrice.multiply(BigDecimal.ONE.subtract(new BigDecimal(marketExpr)));
        return discountValue;
    }
}
