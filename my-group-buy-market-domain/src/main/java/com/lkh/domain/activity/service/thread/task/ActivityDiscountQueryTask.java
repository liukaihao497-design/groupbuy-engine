package com.lkh.domain.activity.service.thread.task;

import com.lkh.domain.activity.adapter.repository.IActivityRepository;
import com.lkh.domain.activity.model.entity.MarketProductEntity;
import com.lkh.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ActivityDiscountQueryTask extends AbstractTask<List<GroupBuyActivityDiscountVO>>{
    private IActivityRepository repository;
    private MarketProductEntity requestparam;
    @Override
    public List<GroupBuyActivityDiscountVO> call() throws Exception {

        List<GroupBuyActivityDiscountVO> result = repository.queryActivityDiscounts(requestparam.getGoodsId(),requestparam.getSource(),requestparam.getChannel());

        return result;
    }
}
