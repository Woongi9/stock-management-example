package com.example.stock_manager.facade;

import com.example.stock_manager.service.OptimisticLickStockService;
import org.springframework.stereotype.Component;

@Component
public class OptimisticLockStockFacade {

  private final OptimisticLickStockService optimisticLickStockService;

  public OptimisticLockStockFacade(OptimisticLickStockService optimisticLickStockService) {
    this.optimisticLickStockService = optimisticLickStockService;
  }

  public void decrease(Long id, int quantity) throws InterruptedException {
    while (true) {
      try {
        optimisticLickStockService.decreaseStock(id, quantity);
      } catch (Exception e) {
        Thread.sleep(20);
      }
    }
  }
}
