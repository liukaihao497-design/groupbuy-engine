package com.lkh.domain.activity.model.valobj;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DiscountTypeEnum {

    BASE(0, "基础优惠"),
    TAG(1, "人群标签"),
    ;
    private Integer dicountType;
    private String info;

    public static DiscountTypeEnum get(Integer code) {
        switch (code) {
            case 0:
                return BASE;
            case 1:
                return TAG;
            default:
                throw  new RuntimeException("err code");
        }
    }
}
