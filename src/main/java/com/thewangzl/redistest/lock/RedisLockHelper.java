package com.thewangzl.redistest.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RedisLockHelper {

    public static final long expire = 10; // sec

    private static final long waitMillisPer = 10 ; // mills

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean tryLock(String key, String value){
        return tryLock(key, value, 1, TimeUnit.MINUTES);
    }

    public boolean tryLock(String key, String value, long timeout, TimeUnit unit){
        long waitMax = unit.toMillis(timeout);
        long waitAlready = 0;
        try {
            while (!redisTemplate.opsForValue().setIfAbsent(key, value,expire, TimeUnit.SECONDS) && waitAlready < waitMax) {
                Thread.sleep(waitMillisPer);
                waitAlready += waitMillisPer;
            }
            if(waitAlready < waitMax){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.err.println(false);
        return false;
    }


    public boolean lock(String key, String value){
        return redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
    }

    public boolean unlock(String key, String value){
        String v = redisTemplate.opsForValue().get(key);
        if(v.equals(value)){
            return redisTemplate.delete(key);
        }
        return false;
    }

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    public void unlock(String key, String value, long delayTime, TimeUnit timeUnit){
        if(delayTime <= 0){
            unlock(key, value);
        }else{
            executorService.schedule(() -> unlock(key, value), delayTime, timeUnit);
        }
    }

}
