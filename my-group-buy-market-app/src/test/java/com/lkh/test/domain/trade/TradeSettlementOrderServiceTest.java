package com.lkh.test.domain.trade;

import com.alibaba.fastjson2.JSON;
import com.lkh.domain.trade.model.entity.TradePaySettlementEntity;
import com.lkh.domain.trade.model.entity.TradePaySuccessEntity;
import com.lkh.domain.trade.service.ITradeSettlementOrderService;
import com.lkh.types.enums.NotifyTaskStatus;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TradeSettlementOrderServiceTest {
    @Resource
 private OkHttpClient okHttpClient ;


    @Resource
    private ITradeSettlementOrderService tradeSettlementOrderService;

    @Test
    public void test_settlementMarketPayOrder() {
        TradePaySuccessEntity tradePaySuccessEntity = new TradePaySuccessEntity();
        tradePaySuccessEntity.setSource("s01");
        tradePaySuccessEntity.setChannel("c01");
        tradePaySuccessEntity.setUserId("xfg07");
        tradePaySuccessEntity.setOutTradeNo("232930861922");
        TradePaySettlementEntity tradePaySettlementEntity = tradeSettlementOrderService.settlementMarketPayOrder(tradePaySuccessEntity);
        log.info("请求参数:{}", JSON.toJSONString(tradePaySuccessEntity));
        log.info("测试结果:{}", JSON.toJSONString(tradePaySettlementEntity));
    }
    @Test
    public void testJob(){
        try {
            Map<String, Integer> result = tradeSettlementOrderService.execSettlementNotifyJob();
            log.info("定时任务，回调通知拼团完结任务 result:{}", com.alibaba.fastjson.JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("定时任务，回调通知拼团完结任务失败", e);
        }
    }
    @Test
    public void testApi() throws IOException {
        RequestBody requestBody = RequestBody.create("{\"teamId\":\"58630127\",\"outTradeNoList\":[\"141310825552\",\"693334192116\",\"232930861922\"]}", MediaType.parse("application/json"));

        Request request = new Request.Builder().url("http://127.0.0.1:8091/api/v1/test/group_buy_notify")
                .addHeader("content-type", "application/json")
                .post(requestBody).build();

            Response response = okHttpClient.newCall(request).execute();
            log.info("结果为:{},{}",response.body().string(),1);
    }

}
