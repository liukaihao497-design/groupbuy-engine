package com.lkh.infrastructure.dao;

import com.lkh.infrastructure.dao.po.GroupBuyOrderList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
* @author DELL
* @description 针对表【group_buy_order_list】的数据库操作Mapper
* @createDate 2026-06-11 11:07:36
* @Entity com.lkh.infrastructure.dao.po.GroupBuyOrderList
*/
@Mapper
public interface GroupBuyOrderListMapper {

    GroupBuyOrderList queryNoPayOrder(String userId,String outTradeNo);

    int insert(GroupBuyOrderList groupBuyOrderList);

    Integer queryUserValidOrderCount(String userId ,Long activityId);

    GroupBuyOrderList query(GroupBuyOrderList req);

    List<GroupBuyOrderList> queryList(GroupBuyOrderList req);
    int setSuccessPayOrderStatus(String userId, String orderId,Integer status,Integer preStatus);

    List<GroupBuyOrderList> queryListByTeamIdAndActivityId(String teamId,String activityId);

    int setSuccessPayOrderStatusAndOutTradeTime(String userId, String orderId, Date outTradeTime, Integer status , Integer preStatus);

    int countAllTeamUserCount(String goodsId);

    List<GroupBuyOrderList> queryUserOrderInfo(@Param("teamIds") List<String> teamIds);

    GroupBuyOrderList queryLatestValidUserTeam(GroupBuyOrderList queryUserTeamListReq);
}




