package com.lkh.domain.activity.service.trial.discountCalculate;

import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public interface DiscountCalculateService {
    // 默认以2位小数进行计算，并使用四舍五入
     MathContext DEFAULT_MATH_CONTEXT = new MathContext(4, RoundingMode.HALF_UP);
    // 计算折扣后的价格并返回
    BigDecimal calculateDiscountPrice(BigDecimal originalPrice, String userId, GroupBuyActivityDiscountVO activity);

    // 计算折扣的价格
    BigDecimal calculateDiscountValue(BigDecimal originalPrice,String marketExpr);

}
