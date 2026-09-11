package com.lkh.domain.trade.adapter.port;

import com.lkh.domain.trade.model.entity.GroupBuyActivityEntity;
import com.lkh.domain.trade.model.entity.NotifyTaskEntity;

public interface IActivityPort {

    GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId);


}
