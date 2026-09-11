/*
package com.lkh.config;


import com.lkh.infrastructure.dcc.DCCService;
import com.lkh.types.annotations.DCCValue;
import com.lkh.types.exception.AppException;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DCCBeanFactory implements BeanPostProcessor {

    private Map<String,Object> dccObjMap = new HashMap<>();
    @Autowired
    private RedissonClient redissonClient;
    private static final String BASE_CONFIG_PATH = "group_buy_market_dcc_";
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Object targetObject = bean;
        if(AopUtils.isAopProxy(bean)){
            targetClass = AopUtils.getTargetClass(bean);
            targetObject = AopProxyUtils.getSingletonTarget(bean);
        }
        if(targetClass != DCCService.class){
            return bean;
        }
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            DCCValue targetAnnotation = field.getAnnotation(DCCValue.class);
            if(targetAnnotation == null){
                continue;
            }
            try{
                String value = targetAnnotation.value();
                String[] splits = value.split("[,:\\s]+");
                String key = BASE_CONFIG_PATH.concat(splits[0]);
                String defaultValue = splits.length == 2 ? splits[1] : null;


                // 设置值
                String setValue = defaultValue;

                RBucket<String> bucket = redissonClient.getBucket(key);
                boolean exists = bucket.isExists();
                if(exists){
                    setValue = bucket.get();
                }else{
                    if(defaultValue == null){
                        String name = field.getName();
                        throw new RuntimeException(name + "DCCValue配置为空");
                    }
                    bucket.set(setValue);
                }


                field.setAccessible(true);

                field.set(targetObject,setValue);

                field.setAccessible(false);
                dccObjMap.put(key,targetObject);
            }catch (RuntimeException e){
                throw e;
            }
            catch (Exception e){
                throw new RuntimeException("DCC配置初始化异常");
            }

        }

        return bean;
    }

    @Bean
    public RTopic dccListenerTopic(){
        RTopic topic = redissonClient.getTopic("group_buy_market_dcc");

        topic.addListener(String.class,(charSequence, s)->{
            // 分割字符串，拿到key和参数
            String[] splits = s.split("[,:\\s]");
            // 拼装key
            String key = BASE_CONFIG_PATH.concat(splits[0]);
            String setValue = splits.length == 2 ? splits[1] : null;
            if(setValue == null){
                throw new RuntimeException("设置参数为空,请传入正确的参数");
            }
            Object targetObject = dccObjMap.get(key);
            if(targetObject == null){
                return ;
            }
            // 找到真正的目标类
            if(AopUtils.isAopProxy(targetObject)){
                targetObject = AopProxyUtils.getSingletonTarget(targetObject);
            }

            RBucket<String> bucket = redissonClient.getBucket(key);
            boolean exists = bucket.isExists();
            if (!exists) return;
            bucket.set(setValue);

            // 拿到splits[0] 字段名
            try{
                String filedName = splits[0];
                Class<?> aClass = targetObject.getClass();
                Field targetField = aClass.getDeclaredField(filedName);

                targetField.setAccessible(true);
                targetField.set(targetObject,setValue);
                targetField.setAccessible(false);
            }catch (Exception e){
                throw new RuntimeException("字段设置异常");
            }



        });
        return topic;
    }
}
*/
