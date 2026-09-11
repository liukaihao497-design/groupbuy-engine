package com.lkh.domain.activity.service.trial.discountCalculate;

import com.lkh.domain.activity.model.valobj.DiscountTypeEnum;
import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.lkh.domain.activity.service.IActivityService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;
@Slf4j
public abstract class AbstractDiscountCalculateService implements DiscountCalculateService {

    @Resource
    private IActivityService activityService;
    private final static int DEFAULT_SCALE = 2;
    public BigDecimal money(BigDecimal amount) {
        if(amount == null) throw new RuntimeException("业务计算异常");
        return amount.setScale(DEFAULT_SCALE, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public BigDecimal calculateDiscountPrice(BigDecimal originalPrice, String userId, GroupBuyActivityDiscountVO activity) {

        // 当前活动配置人群标签
        if (DiscountTypeEnum.TAG.equals(activity.getGroupBuyDiscount().getDiscountType())) {
            //  人流标签过滤
            boolean isVisble = isVisibleByTag(userId, activity.getGroupBuyDiscount().getTagId());
            // 不可见返回原价
            if(!isVisble){
                log.info("不属于折扣目标人群，无折扣");
                return originalPrice;
            }
        }
        // 做优惠计算，由子类实现
        BigDecimal discountPrice = doCalculateDiscountPrice(originalPrice, activity.getGroupBuyDiscount().getMarketExpr());

        return money(discountPrice);
    }

    protected boolean isVisibleByTag(String userId,String discountTag){
        // 参数校验
        if(discountTag == null){
            return true;
        }

        // 人群标签过滤
        boolean b = activityService.isCrowdTagMatch(userId,discountTag);

        return b;
    }
    // 计算逻辑
    protected abstract BigDecimal doCalculateDiscountPrice(BigDecimal originalPrice, String marketExpr) ;
}
