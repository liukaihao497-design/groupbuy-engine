package com.lkh.trigger.http;

import java.util.Date;

import com.lkh.api.IMarketTradeService;
import com.lkh.api.dto.LockMarketPayOrderRequestDTO;
import com.lkh.api.dto.LockMarketPayOrderResponseDTO;
import com.lkh.api.dto.SettlementMarketPayOrderRequestDTO;
import com.lkh.api.dto.SettlementMarketPayOrderResponseDTO;
import com.lkh.api.response.Response;
import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.lkh.domain.activity.service.IActivityService;
import com.lkh.domain.activity.service.IIndexGroupBuyMarketService;
import com.lkh.domain.trade.model.entity.*;
import com.lkh.domain.trade.model.valobj.GroupBuyProgressVO;
import com.lkh.domain.trade.model.valobj.NotifyConfigVO;
import com.lkh.domain.trade.model.valobj.enums.NotifyTypeEnumVO;
import com.lkh.domain.trade.service.ITradeLockOrderService;
import com.lkh.domain.trade.service.ITradeSettlementOrderService;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/trade/")
public class MarketTradeController implements IMarketTradeService {
    @Autowired
    private ITradeLockOrderService tradeService;
    @Autowired
    private ITradeSettlementOrderService tradeSettlementService;
    @Autowired
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;
    @Autowired
    private IActivityService activityService;


    @RequestMapping(value = "lock_market_pay_order", method = RequestMethod.POST)
    @Override
    public Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(@RequestBody LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO) {

        String userId = lockMarketPayOrderRequestDTO.getUserId();
        String teamId = lockMarketPayOrderRequestDTO.getTeamId();
        Long activityId = lockMarketPayOrderRequestDTO.getActivityId();
        String goodsId = lockMarketPayOrderRequestDTO.getGoodsId();
        String source = lockMarketPayOrderRequestDTO.getSource();
        String channel = lockMarketPayOrderRequestDTO.getChannel();
        String outTradeNo = lockMarketPayOrderRequestDTO.getOutTradeNo();
        LockMarketPayOrderRequestDTO.NotifyConfigVO notifyConfigVO = lockMarketPayOrderRequestDTO.getNotifyConfigVO();

        if (StringUtils.isAnyBlank(userId, goodsId, source, channel, outTradeNo)
                || activityId == null || notifyConfigVO == null
                || StringUtils.isBlank(notifyConfigVO.getNotifyType())) {
            return new Response<>(ResponseCode.ILLEGAL_PARAMETER);
        }

        NotifyTypeEnumVO notifyTypeEnumVO;
        try {
            notifyTypeEnumVO = NotifyTypeEnumVO.valueOf(notifyConfigVO.getNotifyType());
        } catch (IllegalArgumentException e) {
            return new Response<>(ResponseCode.ILLEGAL_PARAMETER);
        }
        if (NotifyTypeEnumVO.HTTP.equals(notifyTypeEnumVO)
                && StringUtils.isBlank(notifyConfigVO.getNotifyUrl())) {
            return new Response<>(ResponseCode.ILLEGAL_PARAMETER);
        }



        // 先查询外部订单
        GroupBuyOrderListEntity entity = tradeService.queryNoPayOrder(userId, outTradeNo);
        // 如果订单则封装结果
        if (entity != null) {
            String orderId = entity.getOrderId();
            BigDecimal deductionPrice = entity.getDeductionPrice();
            Integer status = entity.getStatus();
            BigDecimal originalPrice = entity.getOriginalPrice();
            BigDecimal payPrice = entity.getPayPrice();

            Response<LockMarketPayOrderResponseDTO> responseDTOResponse = new Response<>(LockMarketPayOrderResponseDTO.builder()
                    .orderId(orderId)
                    .tradeOrderStatus(status)
                    .payPrice(payPrice)
                    .originalPrice(originalPrice)
                    .deductionPrice(deductionPrice).build());
            return responseDTOResponse;
        }
        if (teamId != null) {
            // 查看拼团是否已经拼团结束
            GroupBuyProgressVO groupBuyProgressVO = tradeService.queryGroupBuyProcess(teamId);
            if (groupBuyProgressVO == null) {
                throw new RuntimeException("拼团状态异常");
            }
            if (groupBuyProgressVO.getTargetCount() <= groupBuyProgressVO.getLockCount()) {
                // 拼团锁单和完成数量大于等于目标数量，没有拼团位置
                return new Response<>(ResponseCode.E0006);
            }
        }


        // 试算优惠
        MarketProductEntity req = MarketProductEntity.builder()
                .userId(userId)
                .goodsId(goodsId)
                .source(source)
                .channel(channel)
                .build();

        TrialBalanceEntity res;
        try {
            res = indexGroupBuyMarketService.indexProductTrial(req);
            if (res == null) {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!res.getIsVisible()) {
            return new Response<>(ResponseCode.E0008);
        }


        GroupBuyActivityDiscountVO activityDiscountVO = activityService.queryGroupBuyActivityDiscount(String.valueOf(activityId));
        activityDiscountVO.setSource(lockMarketPayOrderRequestDTO.getSource());
        activityDiscountVO.setChannel(lockMarketPayOrderRequestDTO.getChannel());
        // 进行锁单
        MarketPayOrderEntity marketPayOrderEntity = tradeService.lockMarketPayOrder(
                UserEntity.builder().userId(userId).build(),

                PayActivityEntity.builder()
                        .teamId(teamId).
                        activityId(activityId)
                        .activityName(activityDiscountVO.getActivityName())
                        .startTime(activityDiscountVO.getStartTime())
                        .endTime(activityDiscountVO.getEndTime())
                        .validTime(activityDiscountVO.getValidTime())
                        .targetCount(activityDiscountVO.getTarget())
                        .notifyConfigVO(
                                NotifyConfigVO.builder()
                                        .notifyUrl(notifyConfigVO.getNotifyUrl())
                                        .notifyType(notifyTypeEnumVO)
                                        .notifyMQ(notifyConfigVO.getNotifyMQ())
                                        .build()
                        )
                        .build(),
                PayDiscountEntity.builder()
                        .source(activityDiscountVO.getSource())
                        .channel(activityDiscountVO.getChannel())
                        .goodsId(activityDiscountVO.getGoodsId())
                        .goodsName(res.getGoodsName())
                        .originalPrice(res.getOriginalPrice())
                        .deductionPrice(res.getDeductionPrice())
                        .payPrice(res.getPayPrice())
                        .outTradeNo(outTradeNo)
                        .build()
        );
        // 封装结果
        LockMarketPayOrderResponseDTO d = LockMarketPayOrderResponseDTO.builder()
                .orderId(marketPayOrderEntity.getOrderId())
                .deductionPrice(marketPayOrderEntity.getDeductionPrice())
                .payPrice(marketPayOrderEntity.getPayPrice())
                .originalPrice(marketPayOrderEntity.getOriginalPrice())
                .tradeOrderStatus(marketPayOrderEntity.getTradeOrderStatusEnumVO().getCode())
                .build();
        return new Response<>(d);
    }


    @RequestMapping(value = "settlement_market_pay_order", method = RequestMethod.POST)
    @Override
    public Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrder(@RequestBody SettlementMarketPayOrderRequestDTO requestDTO) {
       try{
           log.info("营销交易组队结算开始:{} outTradeNo:{}", requestDTO.getUserId(), requestDTO.getOutTradeNo());
           // 参数校验
           if (StringUtils.isBlank(requestDTO.getUserId()) || StringUtils.isBlank(requestDTO.getSource()) || StringUtils.isBlank(requestDTO.getChannel()) || StringUtils.isBlank(requestDTO.getOutTradeNo()) || null == requestDTO.getOutTradeTime()) {
               return Response.<SettlementMarketPayOrderResponseDTO>builder()
                       .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                       .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                       .build();
           }

           String source = requestDTO.getSource();
           String channel = requestDTO.getChannel();
           String userId = requestDTO.getUserId();
           String outTradeNo = requestDTO.getOutTradeNo();
           Date outTradeTime = requestDTO.getOutTradeTime();

           // 构建结算请求参数
           TradePaySuccessEntity settlementRequest = new TradePaySuccessEntity();
           settlementRequest.setSource(source);
           settlementRequest.setChannel(channel);
           settlementRequest.setUserId(userId);
           settlementRequest.setOutTradeNo(outTradeNo);
           settlementRequest.setNotifyUrl("");
           settlementRequest.setOutTradeTime(outTradeTime);

           // 请求交易服务进行结算
           TradePaySettlementEntity tradePaySettlementEntity = tradeSettlementService.settlementMarketPayOrder(settlementRequest);

           // 构建返回数据
           SettlementMarketPayOrderResponseDTO responseDTO = new SettlementMarketPayOrderResponseDTO();
           responseDTO.setUserId(tradePaySettlementEntity.getUserId());
           responseDTO.setTeamId(tradePaySettlementEntity.getTeamId());
           responseDTO.setActivityId(tradePaySettlementEntity.getActivityId());
           responseDTO.setOutTradeNo(tradePaySettlementEntity.getOutTradeNo());


           // 构建响应对象
           Response<SettlementMarketPayOrderResponseDTO> response = new Response<>(responseDTO);

           return response;

       }catch (AppException e) {

           return Response.<SettlementMarketPayOrderResponseDTO>builder()
                   .code(e.getCode())
                   .info(e.getInfo())
                   .data(null).build();
       }catch (Exception e) {

           return Response.<SettlementMarketPayOrderResponseDTO>builder()
                   .code(ResponseCode.UN_ERROR.getCode())
                   .info(ResponseCode.UN_ERROR.getInfo())
                   .data(null).build();
       }


    }


}
