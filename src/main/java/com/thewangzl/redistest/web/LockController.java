package com.thewangzl.redistest.web;

import com.thewangzl.redistest.lock.LockParam;
import com.thewangzl.redistest.lock.RepeatSubmitLock;
import com.thewangzl.redistest.service.StockService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("lock")
public class LockController {

    private Executor executor = Executors.newFixedThreadPool(30);

    @Autowired
    private StockService stockService;

    @PutMapping("stock/{id}/deduct")
    public void stock(@PathVariable String id){
        int size = stockService.getStock();
        for(int i = 0; i< size; i++){
            executor.execute(() -> stockService.deduct(id));
        }
    }

    @GetMapping("stock")
    public int getStock(){
        return stockService.getStock();
    }

    @PutMapping("stock/reset")
    public int reset(){
        stockService.resetStock();
        return stockService.getStock();
    }

    @PostMapping("repeat")
    @RepeatSubmitLock
    public String repeatTest(@LockParam String token){
        return token;
    }

    @PostMapping("repeat2")
    @RepeatSubmitLock
    public User repeatTest2(@RequestBody User user){
        return user;
    }

    @Data
    @NoArgsConstructor
    private static class User{
        @LockParam
        private String userName;
        private String password;
    }

}
