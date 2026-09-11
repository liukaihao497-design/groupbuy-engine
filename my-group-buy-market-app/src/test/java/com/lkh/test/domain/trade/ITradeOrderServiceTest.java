package com.lkh.test.domain.trade;

import com.alibaba.fastjson.JSON;
import com.lkh.api.IMarketTradeService;
import com.lkh.api.dto.LockMarketPayOrderRequestDTO;
import com.lkh.api.dto.LockMarketPayOrderResponseDTO;
import com.lkh.api.response.Response;
import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.lkh.domain.activity.service.IActivityService;
import com.lkh.domain.activity.service.IIndexGroupBuyMarketService;
import com.lkh.domain.trade.model.entity.*;
import com.lkh.domain.trade.model.valobj.NotifyConfigVO;
import com.lkh.domain.trade.model.valobj.enums.NotifyTypeEnumVO;
import com.lkh.domain.trade.service.ITradeLockOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 交易订单服务测试
 * @create 2025-01-11 11:52
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ITradeOrderServiceTest {

    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @Resource
    private ITradeLockOrderService tradeOrderService;
    @Resource
    private IMarketTradeService marketTradeService;
    @Autowired
    private IActivityService activityService;
    @Test
    public void test() throws Exception{
        // 入参信息
        Long activityId = 100123L;
        String userId = "xiaofuge";
        String goodsId = "9890001";
        String source = "s01";
        String channel = "c01";
        String outTradeNo = "909000098114";

    }
    @Test
    public void test_lockMarketPayOrder2() {
        LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO = new LockMarketPayOrderRequestDTO();
        lockMarketPayOrderRequestDTO.setUserId("xfg09");
        lockMarketPayOrderRequestDTO.setTeamId(null);
        lockMarketPayOrderRequestDTO.setActivityId(100123L);
        lockMarketPayOrderRequestDTO.setGoodsId("9890001");
        lockMarketPayOrderRequestDTO.setSource("s01");
        lockMarketPayOrderRequestDTO.setChannel("c01");
        lockMarketPayOrderRequestDTO.setNotifyUrl("http://127.0.0.1:8091/api/v1/test/group_buy_notify");
        lockMarketPayOrderRequestDTO.setOutTradeNo(RandomStringUtils.randomNumeric(12));
        Response<LockMarketPayOrderResponseDTO> lockMarketPayOrderResponseDTOResponse = marketTradeService.lockMarketPayOrder(lockMarketPayOrderRequestDTO);
        log.info("测试结果 req:{} res:{}", JSON.toJSONString(lockMarketPayOrderRequestDTO), JSON.toJSONString(lockMarketPayOrderResponseDTOResponse));
    }
    @Test
    public void test_lockMarketPayOrder() throws Exception {
        // 入参信息
        Long activityId = 100123L;
        String userId = "xiaofuge3";
        String goodsId = "9890001";
        String source = "s01";
        String channel = "c01";
        String outTradeNo = "909000098112";

        // 1. 获取试算优惠，有【activityId】优先使用
        TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexProductTrial(MarketProductEntity.builder()
                .userId(userId)
                .source(source)
                .channel(channel)
                .goodsId(goodsId)
                .build());

        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = activityService.queryGroupBuyActivityDiscount(String.valueOf(activityId));
        // 查询 outTradeNo 是否已经存在交易记录
        GroupBuyOrderListEntity marketPayOrderEntityOld = tradeOrderService.queryNoPayOrder(userId, outTradeNo);
        if (null != marketPayOrderEntityOld) {
            log.info("测试结果(Old):{}", JSON.toJSONString(marketPayOrderEntityOld));
            return;
        }

        // 2. 锁定，营销预支付订单；商品下单前，预购锁定。
        MarketPayOrderEntity marketPayOrderEntityNew = tradeOrderService.lockMarketPayOrder(
                UserEntity.builder().userId(userId).build(),
                PayActivityEntity.builder()
                        .teamId(null)
                        .activityId(groupBuyActivityDiscountVO.getActivityId())
                        .activityName(groupBuyActivityDiscountVO.getActivityName())
                        .startTime(groupBuyActivityDiscountVO.getStartTime())
                        .endTime(groupBuyActivityDiscountVO.getEndTime())
                        .targetCount(groupBuyActivityDiscountVO.getTarget())
                        .notifyConfigVO(NotifyConfigVO.builder()
                                .notifyType(NotifyTypeEnumVO.MQ)
                                .build())
                        .build(),
                PayDiscountEntity.builder()
                        .source(source)
                        .channel(channel)
                        .goodsId(goodsId)
                        .goodsName(trialBalanceEntity.getGoodsName())
                        .originalPrice(trialBalanceEntity.getOriginalPrice())
                        .deductionPrice(trialBalanceEntity.getDeductionPrice())
                        .outTradeNo(outTradeNo)
                        .build());

        log.info("测试结果(New):{}",JSON.toJSONString(marketPayOrderEntityNew));
    }

}
