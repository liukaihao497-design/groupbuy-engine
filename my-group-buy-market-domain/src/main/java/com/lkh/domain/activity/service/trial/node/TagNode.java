package com.lkh.domain.activity.service.trial.node;

import com.lkh.domain.activity.adapter.repository.IActivityRepository;
import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.entity.TrialBalanceEntity;
import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.lkh.domain.activity.model.valobj.TagIdScopeVO;
import com.lkh.domain.activity.model.valobj.TagScopeEnum;
import com.lkh.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.lkh.domain.activity.service.trial.factory.DefaultTrialFactory;
import com.lkh.domain.tag.adapter.repository.ITagRepository;
import com.lkh.types.design.framework.StrategyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * 处理用户是否可见可参加活动
 */
@Service
public class TagNode extends AbstractGroupBuyMarketSupport {
    @Autowired
    private ITagRepository tagRepository;
    @Autowired
    private IActivityRepository activityRepository;
    @Autowired
    private EndNode endNode;
    @Override
    protected void multiThreadLoadContext(MarketProductEntity requestparamj, DefaultTrialFactory.DynamicContext context) throws Exception {

    }

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) throws Exception {

        // 查询活动人群标签id和标签权限
        GroupBuyActivityDiscountVO activityDiscountVO = context.getDiscountVOList().get(0);
        TagIdScopeVO tagIdScopeVO = activityRepository.queryTagIdAndScopeByActivityId(activityDiscountVO.getActivityId());
        // 判断用户是否属于活动人群，若不属于，则判断是否有限制
        String activityTagId = tagIdScopeVO.getTagId();
        String tagScope = tagIdScopeVO.getTagScope();
        if(activityTagId != null && !activityRepository.isTagIdUser(activityTagId,requestparam.getUserId())){
            // 活动有指定人群 && 但用户不属于指定人群
            boolean isVisible = TagScopeEnum.noContain(tagScope, TagScopeEnum.VISIBLE);
            boolean isEnable = TagScopeEnum.noContain(tagScope, TagScopeEnum.ENABLE);
            context.setVisible(isVisible);
            context.setEnable(isEnable);
            return router(requestparam, context);
        }
        // 活动指定人群 && 用户属于指定人群
        context.setVisible(true);
        context.setEnable(true);

        return router(requestparam, context);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultTrialFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestparam, DefaultTrialFactory.DynamicContext context) throws Exception {
        return endNode;
    }
}
