package com.example.stock_manager.service;

import com.example.stock_manager.domain.Stock;
import com.example.stock_manager.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OptimisticLickStockService implements StockService {
  private final StockRepository stockRepository;

  @Override
  @Transactional
  public void decreaseStock(Long stockId, int num) {
    Stock stock = stockRepository.findByIdWithOptimisticLock(stockId);
    stock.decreaseCount(num);
    stockRepository.save(stock);
  }
}
