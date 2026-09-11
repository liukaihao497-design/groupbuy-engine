package com.lkh.domain.activity.adapter.repository;

import com.lkh.domain.activity.model.entity.GroupBuyTeamEntity;
import com.lkh.domain.activity.model.entity.GroupBuyTeamUserEntity;
import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.UserGroupBuyTeamEntity;
import com.lkh.domain.activity.model.valobj.ActivityTeamStatistic;
import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.lkh.domain.activity.model.valobj.RandomTeamInfoQuery;
import com.lkh.domain.activity.model.valobj.TagIdScopeVO;
import org.springframework.stereotype.Component;

import java.util.List;


public interface IActivityRepository {

    List<GroupBuyActivityDiscountVO> queryActivityDiscounts(String goodsId, String source, String channel);

    TagIdScopeVO queryTagIdAndScopeByActivityId(Long activityId);

    boolean isTagIdUser(String activityTagId, String userId);

    boolean isCutRange(String userId);

    boolean isDownGradeSwitch();

    GroupBuyActivityDiscountVO queryActivityDiscounts(String activityId);

    boolean isCrowdTagMatch(String userId, String discountTag);

    String queryActivityIdBySCGoodsId(String source, String channel, String goodsId);

    List<GroupBuyTeamEntity> queryGroupBuyTeamEntity(String source, String channel, String activityId,Integer teamLimit);

    List<GroupBuyTeamUserEntity> queryUserTeamInfo(String teamId,Integer teamUserLimit);

    ActivityTeamStatistic statisticTeam(String source, String channel,String goodsId, String activityId);

    /**
     * 查询用户组队信息
     * @param userId 用户id
     * @param source 来源
     * @param channel 渠道
     * @param goodsId 商品id
     * @return 用户拼团队伍信息
     */
    UserGroupBuyTeamEntity queryUserTeamInfo(String userId, String source, String channel, String goodsId);

    /**
     * 查询指定数量的随机拼团队伍
     * @param randomTeamInfoQuery  查询参数
     * @return 拼团队伍信息集合
     */
    List<UserGroupBuyTeamEntity> queryRandomTeamInfo(RandomTeamInfoQuery randomTeamInfoQuery);
}
