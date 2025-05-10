package com.example.stock_manager.service;

import com.example.stock_manager.domain.Stock;
import com.example.stock_manager.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NamedLockStockService implements StockService {

  private final StockRepository stockRepository;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void decreaseStock(Long stockId, int num) {
    Stock stock = stockRepository.findById(stockId).orElseThrow(() ->
        new IllegalArgumentException("Stock id " + stockId + " not found"));

    stock.decreaseCount(num);
    stockRepository.save(stock);
  }
}
