package com.example.stock_manager.facade;

import com.example.stock_manager.repository.LockRepository;
import com.example.stock_manager.service.NamedLockStockService;
import com.example.stock_manager.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NamedLockStockFacade {

  private final LockRepository lockRepository;
  private final NamedLockStockService stockService;

  @Transactional
  public void decrease(Long id, int quantity) {
    try {
      lockRepository.getLock(id.toString());
      stockService.decreaseStock(id, quantity);
    } finally {
      lockRepository.releaseLock(id.toString());
    }
  }

}
