package com.lkh.api;

import com.lkh.api.dto.LockMarketPayOrderRequestDTO;
import com.lkh.api.dto.LockMarketPayOrderResponseDTO;
import com.lkh.api.dto.SettlementMarketPayOrderRequestDTO;
import com.lkh.api.dto.SettlementMarketPayOrderResponseDTO;
import com.lkh.api.response.Response;

public interface IMarketTradeService {

    Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO);


    /**
     * 营销结算
     *
     * @param requestDTO 结算商品信息
     * @return 结算结果信息
     */
    Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrder(SettlementMarketPayOrderRequestDTO requestDTO);

}