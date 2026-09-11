package com.lkh.infrastructure.utils.mapper;


import com.lkh.domain.activity.model.entity.GroupBuyTeamEntity;
import com.lkh.domain.activity.model.entity.GroupBuyTeamUserEntity;
import com.lkh.domain.tag.model.entity.CrowdTagsJobEntity;
import com.lkh.domain.tag.model.entity.TagDetailEntity;
import com.lkh.domain.trade.model.entity.GroupBuyActivityEntity;
import com.lkh.domain.trade.model.entity.GroupBuyOrderEntity;
import com.lkh.domain.trade.model.entity.GroupBuyOrderListEntity;
import com.lkh.domain.trade.model.entity.NotifyTaskEntity;
import com.lkh.domain.trade.model.valobj.GroupBuyProgressVO;
import com.lkh.infrastructure.dao.po.*;
import com.lkh.types.enums.ActivityStatusEnumVO;
import com.lkh.types.enums.NotifyTaskStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ObjMapper {

    List<TagDetailEntity> toList(List<CrowdTagsDetail> list);


    CrowdTagsJobEntity toEntity(CrowdTagsJob tagsJob);
    GroupBuyOrderListEntity po2Entity(GroupBuyOrderList groupBuyOrderList);

    GroupBuyProgressVO po2VO(GroupBuyOrder res);

    @Mapping(target = "status", source = "status",qualifiedByName = "groupBuyActivityStatusCodeToEnum")
    GroupBuyActivityEntity toEntity(GroupBuyActivity groupBuyActivity);
    @Named("groupBuyActivityStatusCodeToEnum")
    default ActivityStatusEnumVO groupBuyActivityStatusCodeToEnum(Integer status){
        return ActivityStatusEnumVO.valueOf(status);
    }

    GroupBuyOrderListEntity toEntity(GroupBuyOrderList groupBuyOrderList);

    @Mapping(target = "notifyConfigVO", ignore = true)
    GroupBuyOrderEntity toEntity(GroupBuyOrder res);


    @Mapping(target = "notifyMQ", source = "notifyMq")
    NotifyTaskEntity notifyTaskToEntity(NotifyTask notifyTask);

    List<NotifyTaskEntity> notifyTaskListtoEntityList(List<NotifyTask> list);
    default NotifyTaskStatus notifyTaskStatusToEnum(Integer status){
        return NotifyTaskStatus.valueOf(status);
    }

    List<GroupBuyTeamEntity> groupBuyOrderList2GroupBuyTeamEntityList(List<GroupBuyOrder> list);

    List<GroupBuyTeamUserEntity> groupBuyOrderListList2GroupBuyTeamUserEntityList(List<GroupBuyOrderList> list);
}
