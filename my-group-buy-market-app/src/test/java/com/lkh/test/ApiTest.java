package com.lkh.test;

import com.lkh.infrastructure.dao.GroupBuyActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ApiTest {

    @Autowired
    private GroupBuyActivityMapper mapper;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Test
    public void test() {
        String result = redisTemplate.opsForValue().get("liu");

        log.info("{}",result);
    }

}
