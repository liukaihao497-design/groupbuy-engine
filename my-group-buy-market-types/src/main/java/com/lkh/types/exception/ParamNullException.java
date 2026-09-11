package com.lkh.types.exception;

import lombok.Data;

@Data
public class ParamNullException extends RuntimeException {

    /** 异常码 */
    private String code;

    /** 异常信息 */
    private String info;
    public ParamNullException() {

    }
    public ParamNullException(String code,String message) {
        this.code = code;
        this.info = message;
    }

}
