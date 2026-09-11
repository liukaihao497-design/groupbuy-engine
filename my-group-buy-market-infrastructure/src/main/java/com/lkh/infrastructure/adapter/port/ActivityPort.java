package com.lkh.infrastructure.adapter.port;


import com.lkh.domain.trade.model.entity.GroupBuyActivityEntity;
import com.lkh.domain.trade.model.entity.NotifyTaskEntity;
import com.lkh.domain.trade.service.ITradeSettlementOrderService;
import com.lkh.infrastructure.dao.GroupBuyActivityMapper;
import com.lkh.infrastructure.dao.po.GroupBuyActivity;
import com.lkh.infrastructure.gateway.IGroupNotifyService;
import com.lkh.infrastructure.redis.IRedisService;
import com.lkh.infrastructure.utils.mapper.ObjMapper;
import com.lkh.types.enums.NotifyTaskStatus;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.lkh.domain.trade.adapter.port.IActivityPort;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class ActivityPort implements IActivityPort {

    @Resource
    private ObjMapper objMapper;
    @Resource
    private GroupBuyActivityMapper groupBuyActivityMapper;

    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId) {
        GroupBuyActivity groupBuyActivity = groupBuyActivityMapper.queryActivityById(String.valueOf(activityId));
        GroupBuyActivityEntity entity = objMapper.toEntity(groupBuyActivity);
        return entity;
    }


}
