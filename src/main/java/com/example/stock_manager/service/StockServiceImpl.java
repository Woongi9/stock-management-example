package com.example.stock_manager.service;

import com.example.stock_manager.domain.Stock;
import com.example.stock_manager.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

  private final StockRepository stockRepository;

  @Override
//  @Transactional
  public synchronized void decreaseStock(Long stockId, int num) {
    Stock stock = stockRepository.findById(stockId).orElseThrow(() ->
        new IllegalArgumentException("Stock id " + stockId + " not found"));

    stock.decreaseCount(num);
    stockRepository.save(stock);
  }
}
