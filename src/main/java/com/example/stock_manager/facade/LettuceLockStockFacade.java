package com.example.stock_manager.facade;

import com.example.stock_manager.repository.RedisLockRepository;
import com.example.stock_manager.service.NamedLockStockService;
import com.example.stock_manager.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

  private final RedisLockRepository redisLockRepository;
  private final StockService stockService;

  public LettuceLockStockFacade(RedisLockRepository redisLockRepository,
      NamedLockStockService stockService) {
    this.redisLockRepository = redisLockRepository;
    this.stockService = stockService;
  }

  public void decrease(Long id, int quantity) throws InterruptedException {
    while(!redisLockRepository.lock(id)) {
      Thread.sleep(100);
    }

    try {
      stockService.decreaseStock(id, quantity);
    } finally{
      redisLockRepository.unlock(id);
    }
  }
}
