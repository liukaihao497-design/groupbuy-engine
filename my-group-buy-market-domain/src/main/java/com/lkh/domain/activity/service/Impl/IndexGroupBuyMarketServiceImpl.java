package com.lkh.domain.activity.service.Impl;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.lkh.domain.activity.adapter.repository.IActivityRepository;
import com.lkh.domain.activity.model.entity.*;
import com.lkh.domain.activity.model.valobj.ActivityTeamStatistic;
import com.lkh.domain.activity.model.valobj.QueryRandomUserTeamInfoVO;
import com.lkh.domain.activity.model.valobj.RandomTeamInfoQuery;
import com.lkh.domain.activity.service.IActivityService;
import com.lkh.domain.activity.service.IIndexGroupBuyMarketService;
import com.lkh.domain.activity.service.trial.factory.DefaultTrialFactory;
import com.lkh.types.design.framework.StrategyHandler;
import com.lkh.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndexGroupBuyMarketServiceImpl implements IIndexGroupBuyMarketService {

    @Autowired
    private DefaultTrialFactory factory;

    @Autowired
    private IActivityRepository activityRepository;
    @Override
    public TrialBalanceEntity indexProductTrial(MarketProductEntity marketProduct) throws Exception {
        StrategyHandler<MarketProductEntity, DefaultTrialFactory.DynamicContext, TrialBalanceEntity> handler = factory.getTrialHandler();
        TrialBalanceEntity result = handler.apply(marketProduct, new DefaultTrialFactory.DynamicContext());
        // 结果处理
        return result;
    }

    @Override
    public Map<String, List<UserGroupBuyTeamEntity>> queryUserGroupBuyTeamInfo(QueryRandomUserTeamInfoVO vo) {
        String source = vo.getSource();
        String channel = vo.getChannel();
        String goodsId = vo.getGoodsId();
        Integer teamLimit = vo.getTeamLimit();
        Integer teamUserLimit = vo.getTeamUserLimit();

        if(teamLimit == null || teamLimit <= 0){
            teamLimit = 1;
        }
        if(teamUserLimit == null || teamUserLimit <= 0){
            teamUserLimit = 1;
        }

        // 获取活动id
        String activityId = activityRepository.queryActivityIdBySCGoodsId(source,channel,goodsId);
        if(activityId == null){
            return Collections.EMPTY_MAP;
        }
        // 获取当前渠道下指定数量的拼团队伍
        List<GroupBuyTeamEntity> list = activityRepository.queryGroupBuyTeamEntity(source,channel,activityId,teamLimit);
        if(list.isEmpty()){
            return Collections.EMPTY_MAP;
        }
        // 构建返回值
        Map<String, List<UserGroupBuyTeamEntity>> resultMap = new HashMap<>();
        for (GroupBuyTeamEntity groupBuyTeamEntity : list) {
            String teamId = groupBuyTeamEntity.getTeamId();
            Integer targetCount = groupBuyTeamEntity.getTargetCount();
            Integer completeCount = groupBuyTeamEntity.getCompleteCount();
            Integer lockCount = groupBuyTeamEntity.getLockCount();
            Date validStartTime = groupBuyTeamEntity.getValidStartTime();
            Date validEndTime = groupBuyTeamEntity.getValidEndTime();

            List<GroupBuyTeamUserEntity> userEntityList = activityRepository.queryUserTeamInfo(teamId,teamUserLimit);
            List<UserGroupBuyTeamEntity> userGroupBuyTeamEntityArrayList = new ArrayList<>();
            for (GroupBuyTeamUserEntity groupBuyTeamUserEntity : userEntityList) {

                String userId = groupBuyTeamUserEntity.getUserId();
                String outTradeNo = groupBuyTeamUserEntity.getOutTradeNo();


                UserGroupBuyTeamEntity userGroupBuyTeamEntity = new UserGroupBuyTeamEntity();
                userGroupBuyTeamEntity.setUserId(userId);
                userGroupBuyTeamEntity.setTeamId(teamId);
                userGroupBuyTeamEntity.setActivityId(Long.valueOf(activityId));
                userGroupBuyTeamEntity.setTargetCount(targetCount);
                userGroupBuyTeamEntity.setCompleteCount(completeCount);
                userGroupBuyTeamEntity.setLockCount(lockCount);
                userGroupBuyTeamEntity.setValidStartTime(validStartTime);
                userGroupBuyTeamEntity.setValidEndTime(validEndTime);
                userGroupBuyTeamEntity.setValidTimeCountdown(String.valueOf(ChronoUnit.SECONDS.between(validStartTime.toInstant(), validEndTime.toInstant())));
                userGroupBuyTeamEntity.setOutTradeNo(outTradeNo);

                userGroupBuyTeamEntityArrayList.add(userGroupBuyTeamEntity);
            }


            resultMap.put(teamId,userGroupBuyTeamEntityArrayList);
        }

        return resultMap;
    }

    @Override
    public ActivityTeamStatistic queryTeamStatistic(String source, String channel, String goodsId) {
        String activityId = activityRepository.queryActivityIdBySCGoodsId(source, channel, goodsId);
        if(activityId == null){
            return new ActivityTeamStatistic(0,0,0);
        }
        ActivityTeamStatistic statistic = activityRepository.statisticTeam(source,channel,goodsId,activityId);


        return statistic;

    }

    @Override
    public List<UserGroupBuyTeamEntity> queryUserGroupBuyTeamInfoList(QueryRandomUserTeamInfoVO vo) {
        String source = vo.getSource();
        String channel = vo.getChannel();
        String goodsId = vo.getGoodsId();
        Integer teamLimit = vo.getTeamLimit();
        Integer teamUserLimit = vo.getTeamUserLimit();
        String userId = vo.getUserId();

        // 查询用户拼团队伍信息
        UserGroupBuyTeamEntity userTeamInfo = activityRepository.queryUserTeamInfo(userId,source,channel,goodsId);
        Long activityId;
        String teamId;
        if(Objects.isNull(userTeamInfo)){
            // 查询活动id
            String activityIdBySCGoodsId = activityRepository.queryActivityIdBySCGoodsId(source, channel, goodsId);
            if(StringUtils.isBlank(activityIdBySCGoodsId)){
                return Collections.EMPTY_LIST;
            }
            activityId = Long.valueOf(activityIdBySCGoodsId);
            teamId = null;
        }else{
            activityId = userTeamInfo.getActivityId();
            teamId = userTeamInfo.getTeamId();
        }

        // 查询指定数量随机队伍
        RandomTeamInfoQuery randomTeamInfoQuery = new RandomTeamInfoQuery();
        randomTeamInfoQuery.setSource(source);
        randomTeamInfoQuery.setChannel(channel);
        randomTeamInfoQuery.setGoodsId(goodsId);
        randomTeamInfoQuery.setActivityId(activityId);
        randomTeamInfoQuery.setTeamLimit(teamLimit);
        randomTeamInfoQuery.setTeamId(teamId);
        List<UserGroupBuyTeamEntity> list = activityRepository.queryRandomTeamInfo(randomTeamInfoQuery);

        List<UserGroupBuyTeamEntity> resultList = new ArrayList<>();
        if(userTeamInfo != null){
            resultList.add(userTeamInfo);
        }
        resultList.addAll(list);


        return resultList;
    }
}
