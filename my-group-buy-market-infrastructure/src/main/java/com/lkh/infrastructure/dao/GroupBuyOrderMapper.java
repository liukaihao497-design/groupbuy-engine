package com.lkh.infrastructure.dao;

import com.lkh.infrastructure.dao.po.GroupBuyOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author DELL
* @description 针对表【group_buy_order】的数据库操作Mapper
* @createDate 2026-06-11 11:07:36
* @Entity com.lkh.infrastructure.dao.po.GroupBuyOrder
*/
@Mapper
public interface GroupBuyOrderMapper {

    GroupBuyOrder query(GroupBuyOrder req);

    int insert(GroupBuyOrder groupBuyOrder);

    int incyOneCount(String teamId);

    int setSuccessGroupBuyOrderStatus(String teamId,Integer status);

    int incyCompleteCount(String teamId);

    int setSuccessIfCompleteTarget(String teamId ,Integer status);

    List<GroupBuyOrder> queryList(GroupBuyOrder req);

    int countAllTeamNumber(String source, String channel, String activityId);

    int countAllCompleteTeamNumber(String source, String channel, String activityId);

    List<GroupBuyOrder> queryListWithLimit(@Param("req") GroupBuyOrder groupBuyOrderReq,@Param("goodsId") String goodsId,@Param("count") int count);
}




