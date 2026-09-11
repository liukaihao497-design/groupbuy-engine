package com.lkh.infrastructure.adapter.repository;

import com.lkh.domain.activity.adapter.repository.ISkuRepository;
import com.lkh.domain.activity.model.valobj.SkuVO;
import com.lkh.infrastructure.dao.SkuMapper;
import com.lkh.infrastructure.dao.po.Sku;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class SkuRepository implements ISkuRepository {
    @Autowired
    private SkuMapper skuMapper;
    @Override
    public List<SkuVO> querySkus(String goodsId) {
        List<Sku> skus = skuMapper.querySkusByGoodsId(Integer.parseInt(goodsId));
        List<SkuVO> skuVOs = skus.stream().map(
                (sku) -> {
                    SkuVO skuVO = new SkuVO();
                    BeanUtils.copyProperties(sku, skuVO);
                    return skuVO;
                }
        ).collect(Collectors.toList());
        return skuVOs;
    }
}
