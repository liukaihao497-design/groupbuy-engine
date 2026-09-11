package com.lkh.domain.activity.service.Impl;

import com.lkh.domain.activity.adapter.repository.IActivityRepository;
import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.lkh.domain.activity.service.IActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityService implements IActivityService {
    @Autowired
    private IActivityRepository activityRepository;
    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscount(String activityId) {
        GroupBuyActivityDiscountVO vo = activityRepository.queryActivityDiscounts(activityId);
        return vo;
    }

    @Override
    public boolean isCrowdTagMatch(String userId, String discountTag) {

        return  activityRepository.isCrowdTagMatch(userId,discountTag);
    }
}
