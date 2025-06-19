package com.ruoyi.common.redis.config;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisLock {
    @Autowired
    private RedissonClient redissonClient;
    public RLock getRLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    public Boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock rLock = getRLock(lockKey);
        boolean tryLock;
        try {
            tryLock = rLock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            return false;
        }
        return tryLock;
    }

    public void unlock(String lockKey) {
        RLock lock = getRLock(lockKey);

        if(lock.isLocked()){ // 是否还是锁定状态
            if(lock.isHeldByCurrentThread()){ // 时候是当前执行线程的锁
                lock.unlock(); // 释放锁
            }
        }
        // lock.unlock();
    }
}