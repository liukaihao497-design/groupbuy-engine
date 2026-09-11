package com.lkh.infrastructure.dao;

import com.lkh.infrastructure.dao.po.Sku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author DELL
* @description 针对表【sku(商品信息)】的数据库操作Mapper
* @createDate 2026-06-05 17:29:04
* @Entity com.lkh.infrastructure.dao.po.Sku
*/
@Mapper
public interface SkuMapper {

    List<Sku> querySkusByGoodsId(Integer goodsId);
}




