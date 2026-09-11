package com.lkh.domain.activity.adapter.repository;

import com.lkh.domain.activity.model.valobj.SkuVO;
import org.springframework.stereotype.Component;

import java.util.List;


public interface ISkuRepository {

    List<SkuVO> querySkus(String goodsId);

}
