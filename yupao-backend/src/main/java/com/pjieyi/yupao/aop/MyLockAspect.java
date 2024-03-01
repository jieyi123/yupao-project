package com.pjieyi.yupao.aop;

import com.pjieyi.yupao.annotation.MyLock;
import com.pjieyi.yupao.common.ErrorCode;
import com.pjieyi.yupao.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author pjieyi
 * @desc
 */
@Aspect
@Component
public class MyLockAspect implements Ordered {


    @Resource
    private  RedissonClient redissonClient;

    @Around("@annotation(myLock)")
    public Object tryLock(ProceedingJoinPoint pjp, MyLock myLock) throws Throwable{
        //1.创建锁对象
        RLock lock = redissonClient.getLock(myLock.name());
        //2.尝试获取锁
        boolean isLock = lock.tryLock(myLock.waitTime(), myLock.leaseTime(), myLock.unit());
        //判断是否获取到锁
        if (!isLock){
            //没有拿到锁
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"请求太频繁");
        }
        try{
            //获取到锁
            return pjp.proceed();
        }finally {
            //释放自己的锁
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
