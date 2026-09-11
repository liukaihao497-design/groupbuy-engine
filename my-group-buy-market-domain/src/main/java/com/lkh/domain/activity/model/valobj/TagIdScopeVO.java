package com.lkh.domain.activity.model.valobj;

import lombok.Data;

@Data
public class TagIdScopeVO {

    /**
     * 人群标签规则标识
     */
    private String tagId;

    /**
     * 人群标签规则范围（多选；1可见限制、2参与限制）
     */
    private String tagScope;

}
