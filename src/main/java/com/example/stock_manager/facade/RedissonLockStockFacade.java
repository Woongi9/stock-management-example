package com.example.stock_manager.facade;

import com.example.stock_manager.service.NamedLockStockService;
import com.example.stock_manager.service.StockService;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
public class RedissonLockStockFacade {

  private RedissonClient redissonClient;
  private StockService stockService;

  public RedissonLockStockFacade(RedissonClient redissonClient, NamedLockStockService stockService) {
    this.redissonClient = redissonClient;
    this.stockService = stockService;
  }

  public void decrease(Long id, int quantity) {
    RLock lock = redissonClient.getLock(id.toString());
    try {
      boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

      if (!available) {
        System.out.println("lock 획득 실패");
        return;
      }

      stockService.decreaseStock(id, quantity);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }
}
