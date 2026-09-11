package com.lkh.infrastructure.dao;

import com.lkh.infrastructure.dao.po.GroupBuyActivity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author DELL
* @description 针对表【group_buy_activity(拼团活动)】的数据库操作Mapper
* @createDate 2026-06-04 16:43:16
* @Entity com.lkh.infrastructure.dao.po.GroupBuyActivity
*/
@Mapper
public interface GroupBuyActivityMapper {

    List<GroupBuyActivity> queryGroupBuyActivityList();

    GroupBuyActivity queryActivityById(String activityId);
}




