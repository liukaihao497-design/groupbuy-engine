package com.lkh.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RandomTeamInfoQuery {
    /**
     * 来源
     */
    private String source;
    /**
     * 渠道
     */
    private String channel;
    /**
     * 商品id
     */
    private String goodsId;
    /**
     * 活动id
     */
    private Long activityId;
    /**
     * 队伍限制，指定查询出多少队伍
     */
    private Integer teamLimit;
    /**
     * 队伍id，排除指定某个队伍
     */
    private String teamId;

}
