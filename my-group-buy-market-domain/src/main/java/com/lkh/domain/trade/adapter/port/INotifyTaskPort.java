package com.lkh.domain.trade.adapter.port;

import com.lkh.domain.trade.model.entity.NotifyTaskEntity;
import com.lkh.types.enums.NotifyTaskStatus;

import java.util.List;

public interface INotifyTaskPort {
    /**
     * 执行指定回调任务
     * @param notifyTaskEntity
     * @return
     */
    NotifyTaskStatus execSettlementNotify(NotifyTaskEntity notifyTaskEntity);

    List<NotifyTaskEntity> queryNoCompleteORRetryTask();

    /**
     * 查询指定数量的未完成和待重试的任务
     * @param count
     * @return
     */
    List<NotifyTaskEntity> queryNoCompleteORRetryTask(Integer count);

    int setSuccessAndIncryCount(String teamId);

    int setRetryAndIncryCount(String teamId);

    int setFailAndIncryCount(String teamId);

    List<NotifyTaskEntity> queryTaskByTeamId(String teamId);
}
