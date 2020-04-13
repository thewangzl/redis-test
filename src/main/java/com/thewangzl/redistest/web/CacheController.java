package com.thewangzl.redistest.web;

import com.thewangzl.redistest.lock.LockParam;
import com.thewangzl.redistest.lock.RepeatSubmitLock;
import com.thewangzl.redistest.lock.SyncLock;
import com.thewangzl.redistest.service.StockService;
import com.thewangzl.redistest.util.RandomUtil;
import com.thewangzl.redistest.lock.RedisLockHelper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("cache")
public class CacheController {

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @PostMapping("/{key}/save")
    public String save(@PathVariable("key") String key, @RequestBody Object value){
        redisTemplate.opsForValue().set(key,value);
        return key + ":" + value;
    }

    @GetMapping("/{key}")
    public Object get(@PathVariable("key") String key){

        return redisTemplate.opsForValue().get(key);
    }

    @DeleteMapping("/{key}")
    public String delete(@PathVariable("key") String key){
        redisTemplate.delete(key);
        return "delete key:" + key;
    }
}
