package com.lkh.infrastructure.gateway;

import com.lkh.types.enums.NotifyTaskStatus;
import org.springframework.stereotype.Component;


public interface IGroupNotifyService {

    NotifyTaskStatus execNotifyJob(String notifyUrl, String parameterJson);
}
