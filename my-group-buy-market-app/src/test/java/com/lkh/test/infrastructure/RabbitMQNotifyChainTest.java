package com.lkh.test.infrastructure;

import com.lkh.domain.trade.model.entity.NotifyTaskEntity;
import com.lkh.infrastructure.adapter.port.NotifyTaskPort;
import com.lkh.infrastructure.dao.NotifyTaskMapper;
import com.lkh.infrastructure.dao.po.NotifyTask;
import com.lkh.infrastructure.event.EventPublisher;
import com.lkh.infrastructure.gateway.IGroupNotifyService;
import com.lkh.infrastructure.redis.IRedisService;
import com.lkh.infrastructure.utils.mapper.ObjMapper;
import com.lkh.types.enums.NotifyTaskStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.redisson.api.RLock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMQNotifyChainTest {

    @Mock
    private IGroupNotifyService groupNotifyService;
    @Mock
    private IRedisService redisService;
    @Mock
    private NotifyTaskMapper notifyTaskMapper;
    @Mock
    private ObjMapper objMapper;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private RLock lock;

    private NotifyTaskPort notifyTaskPort;

    @Before
    public void setUp() {
        notifyTaskPort = new NotifyTaskPort();
        ReflectionTestUtils.setField(notifyTaskPort, "groupNotifyService", groupNotifyService);
        ReflectionTestUtils.setField(notifyTaskPort, "redisService", redisService);
        ReflectionTestUtils.setField(notifyTaskPort, "notifyTaskMapper", notifyTaskMapper);
        ReflectionTestUtils.setField(notifyTaskPort, "objMapper", objMapper);
        ReflectionTestUtils.setField(notifyTaskPort, "publisher", eventPublisher);
    }

    @Test
    public void shouldPublishMqNotifyTask() throws Exception {
        when(redisService.getLock("notify_job_lock_key_team-001")).thenReturn(lock);
        when(lock.tryLock(3, 0, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isLocked()).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        NotifyTaskEntity task = NotifyTaskEntity.builder()
                .teamId("team-001")
                .notifyType("MQ")
                .notifyMQ("topic.team_success")
                .parameterJson("{\"teamId\":\"team-001\"}")
                .notifyCount(0)
                .notifyStatus(NotifyTaskStatus.create)
                .build();

        NotifyTaskStatus result = notifyTaskPort.execSettlementNotify(task);

        assertEquals(NotifyTaskStatus.complete, result);
        verify(eventPublisher).publish("topic.team_success", "{\"teamId\":\"team-001\"}");
        verify(lock).unlock();
    }

    @Test
    public void shouldMapNotifyMqRoutingKey() {
        ObjMapper mapper = Mappers.getMapper(ObjMapper.class);
        NotifyTask notifyTask = new NotifyTask();
        notifyTask.setTeamId("team-001");
        notifyTask.setNotifyType("MQ");
        notifyTask.setNotifyMq("topic.team_success");
        notifyTask.setNotifyStatus(NotifyTaskStatus.create.getCode());

        NotifyTaskEntity entity = mapper.notifyTaskToEntity(notifyTask);

        assertEquals("topic.team_success", entity.getNotifyMQ());
        assertEquals(NotifyTaskStatus.create, entity.getNotifyStatus());
    }
}

