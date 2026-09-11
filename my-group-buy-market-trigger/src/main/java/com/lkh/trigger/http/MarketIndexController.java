package com.lkh.trigger.http;
import cn.bugstack.wrench.rate.limiter.types.annotations.RateLimiterAccessInterceptor;
import com.google.common.collect.Lists;
import com.lkh.api.dto.GoodsMarketResponseDTO.TeamStatistic;
import com.lkh.api.dto.GoodsMarketResponseDTO.Goods;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lkh.api.IMarketIndexService;
import com.lkh.api.dto.GoodsMarketRequestDTO;
import com.lkh.api.dto.GoodsMarketResponseDTO;
import com.lkh.api.response.Response;
import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.model.entity.UserGroupBuyTeamEntity;
import com.lkh.domain.activity.model.valobj.ActivityTeamStatistic;
import com.lkh.domain.activity.model.valobj.QueryRandomUserTeamInfoVO;
import com.lkh.domain.activity.service.IIndexGroupBuyMarketService;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/index/")
public class MarketIndexController implements IMarketIndexService {

    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

//    @RateLimiterAccessInterceptor(key = "userId", fallbackMethod = "queryGroupBuyMarketConfigFallBack", permitsPerSecond = 1.0d, blacklistCount = 1)
    @RateLimiterAccessInterceptor(key = "userId", fallbackMethod = "queryGroupBuyMarketConfigFallBack", permitsPerSecond = 1.0d, blacklistCount = 1)
    @RequestMapping(value = "query_group_buy_market_config", method = RequestMethod.POST)
    @Override
    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(@RequestBody GoodsMarketRequestDTO goodsMarketRequestDTO) {
        try {
            String userId = goodsMarketRequestDTO.getUserId();
            String source = goodsMarketRequestDTO.getSource();
            String channel = goodsMarketRequestDTO.getChannel();
            String goodsId = goodsMarketRequestDTO.getGoodsId();


            // 构建试算商品请求参数
            MarketProductEntity marketProductEntity = new MarketProductEntity();
            marketProductEntity.setUserId(userId);
            marketProductEntity.setGoodsId(goodsId);
            marketProductEntity.setSource(source);
            marketProductEntity.setChannel(channel);
            // 试算商品对用户的优惠
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexProductTrial(marketProductEntity);

            BigDecimal originalPrice = trialBalanceEntity.getOriginalPrice();
            BigDecimal deductionPrice = trialBalanceEntity.getDeductionPrice();
            BigDecimal payPrice = trialBalanceEntity.getPayPrice();
            Long activityId = trialBalanceEntity.getActivityId();


            // 构建用户商品信息
            GoodsMarketResponseDTO.Goods goods = new GoodsMarketResponseDTO.Goods(
                    goodsId,
                    originalPrice,
                    deductionPrice,
                    payPrice
            );

            // 统计正在参团队伍信息，随机筛选获取前两个
            QueryRandomUserTeamInfoVO req = new QueryRandomUserTeamInfoVO();
            req.setSource(source);
            req.setChannel(channel);
            req.setGoodsId(goodsId);
            req.setTeamLimit(2);
            req.setTeamUserLimit(1);
            req.setUserId(userId);
            List<UserGroupBuyTeamEntity> userGroupBuyTeamEntityList = indexGroupBuyMarketService.queryUserGroupBuyTeamInfoList(req);

            // 构建team集合
            List<GoodsMarketResponseDTO.Team> teamList = new ArrayList<>();
            for (UserGroupBuyTeamEntity userGroupBuyTeamEntity : userGroupBuyTeamEntityList) {
                String currentUserId = userGroupBuyTeamEntity.getUserId();
                String teamId = userGroupBuyTeamEntity.getTeamId();
                Integer targetCount = userGroupBuyTeamEntity.getTargetCount();
                Integer completeCount = userGroupBuyTeamEntity.getCompleteCount();
                Integer lockCount = userGroupBuyTeamEntity.getLockCount();
                Date validStartTime = userGroupBuyTeamEntity.getValidStartTime();
                Date validEndTime = userGroupBuyTeamEntity.getValidEndTime();
                String outTradeNo = userGroupBuyTeamEntity.getOutTradeNo();
                // 构建team
                GoodsMarketResponseDTO.Team team = new GoodsMarketResponseDTO.Team();
                team.setUserId(currentUserId);
                team.setTeamId(teamId);
                team.setActivityId(activityId);
                team.setTargetCount(targetCount);
                team.setCompleteCount(completeCount);
                team.setLockCount(lockCount);
                team.setValidStartTime(validStartTime);
                team.setValidEndTime(validEndTime);
                team.setValidTimeCountdown(
                        GoodsMarketResponseDTO.Team.differenceDateTime2Str(
                        new Date(),
                        validEndTime)
                );
                team.setOutTradeNo(outTradeNo);
                // 添加team
                teamList.add(team);
            }



            // 统计活动历史队伍参与信息
            ActivityTeamStatistic activityTeamStatistic = indexGroupBuyMarketService.queryTeamStatistic(source,channel,goodsId);

            Integer allTeamCount = activityTeamStatistic.getAllTeamCount();
            Integer allTeamCompleteCount = activityTeamStatistic.getAllTeamCompleteCount();
            Integer allTeamUserCount = activityTeamStatistic.getAllTeamUserCount();

            TeamStatistic teamStatistic = new TeamStatistic();
            teamStatistic.setAllTeamCount(allTeamCount);
            teamStatistic.setAllTeamCompleteCount(allTeamCompleteCount);
            teamStatistic.setAllTeamUserCount(allTeamUserCount);


            // 构建返回参数
            GoodsMarketResponseDTO goodsMarketResponseDTO = new GoodsMarketResponseDTO();
            goodsMarketResponseDTO.setActivityId(activityId);
            goodsMarketResponseDTO.setGoods(goods);
            goodsMarketResponseDTO.setTeamList(teamList);
            goodsMarketResponseDTO.setTeamStatistic(teamStatistic);


            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(goodsMarketResponseDTO).build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }


    }


    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfigFallBack(@RequestBody GoodsMarketRequestDTO requestDTO) {
        log.error("查询拼团营销配置限流:{}", requestDTO.getUserId());
        return Response.<GoodsMarketResponseDTO>builder()
                .code(ResponseCode.RATE_LIMITER.getCode())
                .info(ResponseCode.RATE_LIMITER.getInfo())
                .build();
    }

}
