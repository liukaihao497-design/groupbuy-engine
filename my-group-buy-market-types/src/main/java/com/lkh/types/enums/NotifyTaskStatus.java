package com.lkh.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum NotifyTaskStatus {

    create(0, "创建"),
    complete(1,"完成"),
    retry(2,"重试"),
    fail(3,"失败")
    ;

    private Integer code;
    private String info;

    public static NotifyTaskStatus valueOf(Integer code) {
        for (NotifyTaskStatus value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        throw new RuntimeException("err code not exist!");
    }
}
