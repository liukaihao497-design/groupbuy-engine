package com.lkh.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryRandomUserTeamInfoVO {
    /**
     * 来源
     */
    private String source;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 商品ID
     */
    private String goodsId;

    /**
     * 随机查询多少个队伍
     */
    private Integer teamLimit;
    /**
     * 队伍下的用户数量
     */
    private Integer teamUserLimit;

    /**
     * 当前用户
     */
    private String userId;
}
