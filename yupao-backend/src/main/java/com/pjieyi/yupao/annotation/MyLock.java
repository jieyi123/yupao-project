package com.pjieyi.yupao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author pjieyi
 * @desc 自定义Redisson注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyLock {

    String name();
    long waitTime() default 1;
    long leaseTime() default -1;

    TimeUnit unit() default TimeUnit.MILLISECONDS;
}
