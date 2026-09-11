package com.lkh.api;

import com.lkh.api.response.Response;

public interface IDCCService {
    Response<Boolean> updateDCCValue(String key, String value);
}
