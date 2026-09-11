package com.lkh.infrastructure.adapter.port;

import com.lkh.domain.trade.adapter.port.INotifyTaskPort;
import com.lkh.domain.trade.model.entity.NotifyTaskEntity;
import com.lkh.domain.trade.model.valobj.enums.NotifyTypeEnumVO;
import com.lkh.infrastructure.dao.NotifyTaskMapper;
import com.lkh.infrastructure.dao.po.NotifyTask;
import com.lkh.infrastructure.event.EventPublisher;
import com.lkh.infrastructure.gateway.IGroupNotifyService;
import com.lkh.infrastructure.redis.IRedisService;
import com.lkh.infrastructure.utils.mapper.ObjMapper;
import com.lkh.types.enums.NotifyTaskHTTPEnumVO;
import com.lkh.types.enums.NotifyTaskStatus;
import com.lkh.types.enums.ResponseCode;
import com.lkh.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class NotifyTaskPort implements INotifyTaskPort{
    @Resource
    private IGroupNotifyService groupNotifyService;
    @Resource
    private IRedisService redisService;
    @Resource
    private NotifyTaskMapper notifyTaskMapper;

    @Resource
    private ObjMapper objMapper;
    @Resource
    private EventPublisher publisher;
    /**
     * 任务无执行则返回null ，任务正常执行后则返回执行后任务状态
     * @param notifyTaskEntity
     * @return
     */
    @Override
    public NotifyTaskStatus execSettlementNotify(NotifyTaskEntity notifyTaskEntity) {
        RLock lock = redisService.getLock(notifyTaskEntity.lockKey());
        try {
            if (lock.tryLock(3, 0, TimeUnit.SECONDS)) {
                try{
                   /* String notifyUrl = notifyTaskEntity.getNotifyUrl();
                    String parameterJson = notifyTaskEntity.getParameterJson();
                    if(StringUtils.isBlank(notifyUrl) || "暂无".equals(notifyUrl)){
                        return NotifyTaskStatus.fail;
                    }
                    // 执行任务
                    NotifyTaskStatus status = groupNotifyService.execNotifyJob(notifyUrl,parameterJson);
                    return status;

*/
                    // 回调方式 HTTP
                    if (NotifyTypeEnumVO.HTTP.getCode().equals(notifyTaskEntity.getNotifyType())) {
                        // 无效的 notifyUrl 则直接返回成功
                        if (StringUtils.isBlank(notifyTaskEntity.getNotifyUrl()) || "暂无".equals(notifyTaskEntity.getNotifyUrl())) {
                            return NotifyTaskStatus.fail;
                        }
                        String notifyUrl = notifyTaskEntity.getNotifyUrl();
                        String parameterJson = notifyTaskEntity.getParameterJson();
                        // 执行任务
                        NotifyTaskStatus status = groupNotifyService.execNotifyJob(notifyUrl,parameterJson);
                        return status;   }

                    // 回调方式 MQ
                    if (NotifyTypeEnumVO.MQ.getCode().equals(notifyTaskEntity.getNotifyType())) {
                        if (StringUtils.isBlank(notifyTaskEntity.getNotifyMQ())) {
                            log.error("MQ通知任务缺少路由键 teamId:{}", notifyTaskEntity.getTeamId());
                            return NotifyTaskStatus.fail;
                        }
                        publisher.publish(notifyTaskEntity.getNotifyMQ(), notifyTaskEntity.getParameterJson());
                        return NotifyTaskStatus.complete;
                    }

                    log.error("通知任务类型无效 teamId:{} notifyType:{}", notifyTaskEntity.getTeamId(), notifyTaskEntity.getNotifyType());
                    return NotifyTaskStatus.fail;
                }finally {
                    if(lock.isLocked() && lock.isHeldByCurrentThread()){
                        lock.unlock();
                    }
                }
            }

        return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("任务{}出现打断异常",notifyTaskEntity.getId(), e);
            return null;
        }catch (Exception e){
            log.error("任务{}出现非打断异常",notifyTaskEntity.getId(),e);
            return NotifyTaskStatus.retry;
        }

    }

    @Override
    public List<NotifyTaskEntity> queryNoCompleteORRetryTask() {
        // 默认查询15个
        return queryNoCompleteORRetryTask(15);
    }

    @Override
    public List<NotifyTaskEntity> queryNoCompleteORRetryTask(Integer count) {
        List<NotifyTask> list = notifyTaskMapper.queryNoCompleteORRetryTask(count,NotifyTaskStatus.create.getCode(),NotifyTaskStatus.retry.getCode());
        List<NotifyTaskEntity> entityList = objMapper.notifyTaskListtoEntityList(list);
        return entityList;
    }

    @Override
    public int setSuccessAndIncryCount(String teamId) {

        return notifyTaskMapper.setSuccessAndIncryCount(teamId);
    }

    @Override
    public int setRetryAndIncryCount(String teamId) {

        return notifyTaskMapper.setRetryAndIncryCount(teamId);
    }

    @Override
    public int setFailAndIncryCount(String teamId) {

        return notifyTaskMapper.setFailAndIncryCount(teamId);
    }

    @Override
    public List<NotifyTaskEntity> queryTaskByTeamId(String teamId) {
        NotifyTask notifyTask = notifyTaskMapper.queryByTeamId(teamId);
        if (notifyTask == null) {
            return Collections.emptyList();
        }
        return objMapper.notifyTaskListtoEntityList(Collections.singletonList(notifyTask));
    }

    private NotifyTaskStatus checkNotifyJobStatus(String teamId) {
        NotifyTask task = notifyTaskMapper.queryByTeamId(teamId);
        if(task == null) {
            throw new AppException(ResponseCode.E0118);
        }
        Integer notifyStatus = task.getNotifyStatus();
        return NotifyTaskStatus.valueOf(notifyStatus);
    }
}
