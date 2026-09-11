package com.lkh.domain.trade.model.entity;

import com.lkh.types.enums.NotifyTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotifyTaskEntity {



    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 拼单组队ID
     */
    private String teamId;

    /**
     * 回调类型
     */
    private String notifyType;
    /**
     * 回调消息
     */
    private String notifyMQ;
    /**
     * 回调接口
     */


    /**
     * 回调接口
     */
    private String notifyUrl;

    /**
     * 回调次数
     */
    private Integer notifyCount;

    /**
     * 回调状态【0初始、1完成、2重试、3失败】
     */
    private NotifyTaskStatus notifyStatus;

    /**
     * 参数对象
     */
    private String parameterJson;

    // 生成任务唯一key
    public String lockKey(){
        return "notify_job_lock_key_" + teamId;
    }

}
