package com.lkh.domain.activity.service.thread.task;

import com.lkh.domain.activity.adapter.repository.ISkuRepository;
import com.lkh.domain.activity.model.valobj.SkuVO;
import lombok.AllArgsConstructor;

import java.util.List;
@AllArgsConstructor
public class SkuVOQueryTask extends AbstractTask<List<SkuVO>> {
    private ISkuRepository repository;
    private String goodsId;
    @Override
    public List<SkuVO> call() throws Exception {
        List<SkuVO> result = repository.querySkus(goodsId);
        return result;
    }
}

