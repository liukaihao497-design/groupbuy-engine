package com.lkh.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GroupBuyOrderStatus {

    create(0, "拼单中"),
    complete(1,"完成"),
    fail(2,"失败")
    ;

    private Integer code;
    private String info;

    public static GroupBuyOrderStatus valueOf(Integer code) {
        for (GroupBuyOrderStatus value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new RuntimeException("err code not exist!");
    }
}
