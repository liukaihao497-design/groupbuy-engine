package com.lkh.api;

import com.lkh.api.dto.GoodsMarketRequestDTO;
import com.lkh.api.dto.GoodsMarketResponseDTO;
import com.lkh.api.response.Response;

public interface IMarketIndexService {

    /**
     * 查询拼团营销配置
     *
     * @param goodsMarketRequestDTO 营销商品信息
     * @return 营销配置信息
     */
    Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(GoodsMarketRequestDTO goodsMarketRequestDTO);


}
