package com.example.stock_manager.repository;

import com.example.stock_manager.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StockRepository extends JpaRepository<Stock, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Stock s where s.id = :id")
  Stock findByIdWithPessimisticLock(Long id);

  @Lock(LockModeType.OPTIMISTIC)
  @Query("select s from Stock s where s.id = :id")
  Stock findByIdWithOptimisticLock(Long id);
}
