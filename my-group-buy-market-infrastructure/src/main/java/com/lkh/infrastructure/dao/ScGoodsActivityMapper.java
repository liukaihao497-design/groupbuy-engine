package com.lkh.infrastructure.dao;

import com.lkh.infrastructure.dao.po.GroupBuyActivity;
import com.lkh.infrastructure.dao.po.ScGoodsActivity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author DELL
* @description 针对表【sc_goods_activity】的数据库操作Mapper
* @createDate 2026-06-08 15:50:01
* @Entity com.lkh.infrastructure.dao.po.ScGoodsActivity
*/
@Mapper
public interface ScGoodsActivityMapper {

    /**
     * 查询指定渠道下指定商品的活动，通过联表实现，sc渠道表联合活动表
     * @param goodsId
     * @param source
     * @param channel
     * @return
     */
    GroupBuyActivity queryGroupBuyActivity(String goodsId, String source, String channel);

    ScGoodsActivity query(ScGoodsActivity req);

    ScGoodsActivity querySCByActivityId(String activityId);

    String queryActivityId(String source,String channel,String goodsId);
}




