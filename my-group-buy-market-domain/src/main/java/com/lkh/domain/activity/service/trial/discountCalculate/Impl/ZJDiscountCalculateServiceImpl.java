package com.lkh.domain.activity.service.trial.discountCalculate.Impl;

import com.lkh.domain.activity.service.trial.discountCalculate.AbstractDiscountCalculateService;
import com.lkh.domain.activity.service.trial.discountCalculate.DiscountCalculateService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Service("Zj")
public class ZJDiscountCalculateServiceImpl extends AbstractDiscountCalculateService {
    @Override
    public BigDecimal doCalculateDiscountPrice(BigDecimal originalPrice, String marketExpr) {
        //参数校验 TODO

        //计算优惠金额
        BigDecimal discountValue = calculateDiscountValue(originalPrice, marketExpr);
        //计算 展示/折扣后 金额
        BigDecimal discountPrice = originalPrice.subtract(discountValue);
        //结果处理
        return money(discountPrice);
    }

    @Override
    public BigDecimal calculateDiscountValue(BigDecimal originalPrice, String marketExpr) {
        BigDecimal discountValue = new BigDecimal(marketExpr);
        return discountValue;
    }
}
