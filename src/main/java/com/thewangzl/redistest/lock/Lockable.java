package com.thewangzl.redistest.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@interface Lockable {

    long expire() default 10L;

    TimeUnit expireTimeUnit() default TimeUnit.SECONDS;

    long waitTimeout() default 10L;

    TimeUnit waitTimeUnit() default TimeUnit.SECONDS;
}
