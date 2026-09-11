package com.lkh.domain.activity.service;

import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.model.entity.UserGroupBuyTeamEntity;
import com.lkh.domain.activity.model.valobj.ActivityTeamStatistic;
import com.lkh.domain.activity.model.valobj.QueryRandomUserTeamInfoVO;
import com.lkh.domain.activity.service.trial.factory.DefaultTrialFactory;

import java.util.List;
import java.util.Map;

public interface IIndexGroupBuyMarketService {

    TrialBalanceEntity indexProductTrial
            (MarketProductEntity marketProduct) throws Exception;

    /**
     *  查询用户拼团信息
     * @return teamlimit，teamuserlimit source channel goodsid -> activityid  ac sc -> teams -> teams -> team_user
     */
//    UserGroupBuyTeamEntity queryRandomUserTeamInfo();
    Map<String, List<UserGroupBuyTeamEntity>> queryUserGroupBuyTeamInfo(QueryRandomUserTeamInfoVO vo);

    ActivityTeamStatistic queryTeamStatistic(String source, String channel, String goodsId);

    /**
     * 查询用户拼团信息，并随机筛选两个其他拼团信息
     * @param vo
     * @return
     */
    List<UserGroupBuyTeamEntity> queryUserGroupBuyTeamInfoList(QueryRandomUserTeamInfoVO vo);
}
