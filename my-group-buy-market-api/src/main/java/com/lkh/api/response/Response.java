package com.lkh.api.response;

import com.lkh.types.enums.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 7000723935764546321L;

    private String code;
    private String info;
    private T data;

    public Response(T data) {
        this.code = ResponseCode.SUCCESS.getCode();
        this.info = ResponseCode.SUCCESS.getInfo();
        this.data = data;
    }
    public Response(ResponseCode code) {
        this(code.getCode(),code.getInfo(),null);
    }
    public Response(ResponseCode code, T data) {
        this(code.getCode(),code.getInfo(),data);
    }
}
