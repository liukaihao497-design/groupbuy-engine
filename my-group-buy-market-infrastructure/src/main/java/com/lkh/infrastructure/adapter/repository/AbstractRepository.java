package com.lkh.infrastructure.adapter.repository;

import com.lkh.infrastructure.dcc.DCCService;
import com.lkh.infrastructure.redis.IRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.annotation.Resource;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractRepository {


    private final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    @Resource
    protected DCCService dccService;

    @Resource
    protected IRedisService redisService;
    /**
     * 先查缓存，缓存不存在，则查询数据库
     * @param redisCacheKey 缓存key值
     * @param fallback 降级处理，如果缓存不存在，则降级处理
     * @return
     * @param <T> 返回值
     */
    public <T>T getFromCacheOrDb(String redisCacheKey, Supplier<T> fallback){

        T result = null;
        // 判断是否降级处理
        if(dccService.isCacheOpenSwitch()){
            // 正常处理，先查缓存再查数据库
            result = redisCacheKey == null ? null : redisService.getValue(redisCacheKey);
            if(Objects.nonNull(result)){

                return result;
            }
            // 缓存不存在，则使用降级处理方法查询数据
            result = fallback.get();
            if(Objects.nonNull(result)){
                redisService.setValue(redisCacheKey, result);
            }
        }else {
            // 降级处理
            logger.info("缓存降级处理"+redisCacheKey);
            result = fallback.get();
        }

        return result;

    }
}
