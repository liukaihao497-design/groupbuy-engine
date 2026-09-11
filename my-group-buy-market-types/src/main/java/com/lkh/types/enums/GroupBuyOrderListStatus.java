package com.lkh.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GroupBuyOrderListStatus {

    create(0, "初始锁定"),
    payed(1,"消费完成"),
    chargeback(2,"用户退单")
    ;

    private Integer code;
    private String info;

    public static GroupBuyOrderListStatus valueOf(Integer code) {
        for (GroupBuyOrderListStatus value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new RuntimeException("err code not exist!");
    }
}
