package site.bannabe.server.global.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

  String key();

  TimeUnit timeUnit() default TimeUnit.SECONDS;

  long waitTime() default 5L; // 락 획득 대기 시간

  long leaseTime() default 3L; // 락 유지 시간 

}