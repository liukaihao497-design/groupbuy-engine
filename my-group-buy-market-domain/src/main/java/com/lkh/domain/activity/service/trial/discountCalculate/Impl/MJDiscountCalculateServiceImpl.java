package com.lkh.domain.activity.service.trial.discountCalculate.Impl;

import com.lkh.domain.activity.service.trial.discountCalculate.AbstractDiscountCalculateService;
import com.lkh.domain.activity.service.trial.discountCalculate.DiscountCalculateService;
import com.lkh.types.exception.AppException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("MJ")
public class MJDiscountCalculateServiceImpl extends AbstractDiscountCalculateService {
    @Override
    public BigDecimal doCalculateDiscountPrice(BigDecimal originalPrice, String marketExpr) {
        BigDecimal dicountValue = calculateDiscountValue(originalPrice, marketExpr);
        BigDecimal resultPrice = originalPrice.subtract(dicountValue);
        return resultPrice;
    }


    @Override
    public BigDecimal calculateDiscountValue(BigDecimal originalPrice, String marketExpr)  {
        String[] split = marketExpr.split("[,;\\s]+");
        //判断商品价格是否达到满减优惠的金额
        if(originalPrice.compareTo(new BigDecimal(split[0])) < 0){
            return BigDecimal.ZERO;
        }
        
        return new BigDecimal(split[1]);
    }
}
