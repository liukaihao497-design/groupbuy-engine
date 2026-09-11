package com.lkh.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityTeamStatistic {
    // 开团数量
    private Integer allTeamCount;
    // 成团数量
    private Integer allTeamCompleteCount;
    // 参团人数总量
    private Integer allTeamUserCount;
}
