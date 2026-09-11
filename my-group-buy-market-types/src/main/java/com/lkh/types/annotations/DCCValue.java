package com.lkh.types.annotations;

import java.lang.annotation.*;


/**
 * DCCvalue注解 通过配置value，可以实现自动配置
 * 格式为 value1:value2 value1为字段名，value2为默认值，配置完成后，在redis生成对应的key，通过修改
 * redis的value通过发布订阅实现动态更新
 */
/*
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface DCCValue {
    String value() default "";
}
*/
