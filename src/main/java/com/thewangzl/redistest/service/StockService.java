package com.thewangzl.redistest.service;

import com.thewangzl.redistest.lock.LockParam;
import com.thewangzl.redistest.lock.SyncLock;
import org.springframework.stereotype.Service;

@Service
public class StockService {

    private static final int total = 100000;
    private int stock = total;

    public int getStock() {
        return stock;
    }

    @SyncLock(key = "stock")
    public void deduct(@LockParam String id){
        stock--;
    }

    public void resetStock(){
        stock = total;
    }
}
