package com.example.stock_manager.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.stock_manager.domain.Stock;
import com.example.stock_manager.repository.StockRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("재고 Pessimistic Lock 테스트")
public class PessimisticLockTest {

  private Long 딸기_ID;

  @Autowired
  StockRepository stockRepository;
  @Autowired
  PessimisticLockStockServiceImpl stockService;

  @BeforeEach
  void setUp() {
    Stock 딸기Builder = Stock.builder()
        .name("딸기")
        .count(100)
        .build();
    Stock 딸기 = stockRepository.save(딸기Builder);
    딸기_ID = 딸기.getId();
  }

  @AfterEach
  void rollback() {
    stockRepository.deleteAll();
  }

  @Test
  void 한개_차감() {
    stockService.decreaseStock(딸기_ID, 1);

    Stock stock = stockRepository.findById(딸기_ID).orElseThrow();
    assertThat(stock.getCount()).isEqualTo(100 - 1);
  }

  @Test
  void 동시에_100개_요청() throws InterruptedException {
    int threadCount = 100;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch countDownLatch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executorService.submit(() -> {
        try {
          stockService.decreaseStock(딸기_ID, 1);
        } finally {
          countDownLatch.countDown();
        }
      });
    }

    countDownLatch.await();
    Stock stock = stockRepository.findById(딸기_ID).orElseThrow();
    assertThat(stock.getCount()).isEqualTo(0);
  }

}
