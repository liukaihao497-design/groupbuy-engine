package com.lkh.domain.activity.service.trial.node;

import com.alibaba.fastjson.JSON;
import com.lkh.domain.activity.adapter.repository.IActivityRepository;
import com.lkh.domain.activity.adapter.repository.ISkuRepository;
import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.lkh.domain.activity.model.valobj.SkuVO;
import com.lkh.domain.activity.service.thread.task.ActivityDiscountQueryTask;
import com.lkh.domain.activity.service.thread.task.SkuVOQueryTask;
import com.lkh.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.lkh.domain.activity.service.trial.discountCalculate.DiscountCalculateService;
import com.lkh.domain.activity.service.trial.discountCalculate.factory.DefaultDicountCalculateFactroy;
import com.lkh.domain.activity.service.trial.factory.DefaultTrialFactory;
import com.lkh.types.design.framework.StrategyHandler;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class MarketTrialNode extends AbstractGroupBuyMarketSupport {
    @Resource
    private TagNode tagNode;
    @Resource
    private ErrorNode errorNode;
    @Autowired
    private DefaultDicountCalculateFactroy defaultDicountCalculateFactroy;
    @Autowired
    private ISkuRepository skuRepository;
    @Autowired
    private IActivityRepository activityRepository;
    @Autowired
    private ThreadPoolExecutor pool;

    @Override
    public StrategyHandler<MarketProductEntity, DefaultTrialFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) {
        if(Collections.isEmpty(context.getDiscountVOList()) || selectGroupBuyActivityDiscountVO(context.getDiscountVOList()).getGroupBuyDiscount() == null){
            return errorNode;
        }
        return tagNode;
    }

    @Override
    protected void multiThreadLoadContext(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) throws TimeoutException, ExecutionException, InterruptedException {
        //加载商品id 对应的sku列表
        SkuVOQueryTask skuVOQueryTask = new SkuVOQueryTask(skuRepository,requestparam.getGoodsId());

        Future<List<SkuVO>> skuList = pool.submit(skuVOQueryTask);

        //加载商品id 对应的折扣关系
        ActivityDiscountQueryTask discountVOQueryTask = new ActivityDiscountQueryTask(activityRepository, requestparam);
        Future<List<GroupBuyActivityDiscountVO>> discountList = pool.submit(discountVOQueryTask);

            List<SkuVO> skuVOS = skuList.get(skuVOQueryTask.getTimeout(),skuVOQueryTask.getUnit());
            List<GroupBuyActivityDiscountVO> discountVOS = discountList.get(discountVOQueryTask.getTimeout(), discountVOQueryTask.getUnit());
            // 填充上下文
            context.setDiscountVOList(discountVOS);
            context.setSkuVOS(skuVOS);

    }

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) throws Exception{
        log.info("拼团商品查询试算服务-MarketNode userId:{} requestParameter:{}", requestparam.getUserId(), JSON.toJSONString(requestparam));
        if(Collections.isEmpty(context.getDiscountVOList())){
            // 当前商品没活动
            return router(requestparam, context);
        }
        GroupBuyActivityDiscountVO activityDiscountVO = selectGroupBuyActivityDiscountVO(context.getDiscountVOList());
        if(activityDiscountVO == null){
            // 有活动没折扣
            return router(requestparam, context);
        }
        SkuVO skuVO = selectSkuVO(context.getSkuVOS());

        // 取出对应折扣配置的计算器
        DiscountCalculateService discountCalculateService = defaultDicountCalculateFactroy.getDiscountCalculateService(activityDiscountVO.getGroupBuyDiscount().getMarketPlan());
        // 放入计算表达式计算折扣后价格
        BigDecimal resultPrice = discountCalculateService.calculateDiscountPrice(skuVO.getOriginalPrice(), requestparam.getUserId(), selectGroupBuyActivityDiscountVO(context.getDiscountVOList()));

        //参数校验
        BigDecimal tem;
        if(resultPrice.compareTo(tem = new BigDecimal(0)) <= 0){
            resultPrice = tem;
        }
        // 填充上下文
        context.setDeductionPrice(skuVO.getOriginalPrice().subtract(resultPrice));
        context.setPayPrice(resultPrice);
        return router(requestparam, context);
    }

    private GroupBuyActivityDiscountVO selectGroupBuyActivityDiscountVO(List<GroupBuyActivityDiscountVO> discountVOList){
        //根据规则选出sku合适的活动 TODO
        return discountVOList.get(0);
    }
    private SkuVO selectSkuVO(List<SkuVO> skuVOList){
        //根据规则选出sku TODO
        return skuVOList.get(0);
    }

}
