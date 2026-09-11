package com.lkh.infrastructure.gateway.impl;

import com.lkh.infrastructure.gateway.IGroupNotifyService;
import com.lkh.types.enums.NotifyTaskStatus;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class GroupNotifyService implements IGroupNotifyService {

    @Resource
    OkHttpClient okHttpClient;

    @Override
    public NotifyTaskStatus execNotifyJob(String notifyUrl, String parameterJson) {
        RequestBody requestBody = RequestBody.create(parameterJson, MediaType.parse("application/json"));

        Request request = new Request.Builder().url(notifyUrl)
                .addHeader("content-type", "application/json")
                .post(requestBody).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if(response.body() == null){
               return NotifyTaskStatus.retry;
            }
            String string = response.body().string();
            if("success".equals(string)){
                return NotifyTaskStatus.complete;
            } else if ("error".equals(string)) {
                return NotifyTaskStatus.retry;
            }
            return NotifyTaskStatus.valueOf(Integer.parseInt(string));
        }
        catch (NumberFormatException e){
            log.error("返回参数不为整形");
            throw new AppException(ResponseCode.INT_FORMAT_ERROR);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }catch (Exception e){
            log.error("拼团回调 HTTP 接口服务异常 {}", notifyUrl, e);
            throw new AppException(ResponseCode.HTTP_EXCEPTION);
        }

    }
}
