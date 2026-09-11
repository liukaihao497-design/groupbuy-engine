package com.lkh.domain.activity.service;

import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

public interface IActivityService {
    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscount(String activityId);

    boolean isCrowdTagMatch(String userId, String discountTag);
}
