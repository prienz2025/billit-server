package site.bannabe.server.global.aop;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import site.bannabe.server.global.exceptions.BannabeServiceException;
import site.bannabe.server.global.exceptions.ErrorCode;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {

  private static final String REDISSON_LOCK_PREFIX = "LOCK:";

  private final RedissonClient redissonClient;

  private final AopForTransaction aopForTransaction;

  @Around("@annotation(site.bannabe.server.global.aop.DistributedLock)")
  public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

    String key = createKey(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
    RLock lock = redissonClient.getLock(key);

    try {
      boolean available = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
      if (!available) {
        throw new BannabeServiceException(ErrorCode.LOCK_CONFLICT);
      }
      return aopForTransaction.proceed(joinPoint);
    } catch (InterruptedException e) {
      throw new BannabeServiceException(ErrorCode.LOCK_ACQUISITION_FAILED);
    } finally {
      try {
        lock.unlock();
      } catch (IllegalMonitorStateException e) {
        log.info("Redisson Lock Already UnLock {} {}", method.getName(), key);
      }
    }
  }


  private String createKey(String[] parameterNames, Object[] args, String key) {
    return REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(parameterNames, args, key);
  }
}