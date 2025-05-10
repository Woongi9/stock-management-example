package com.example.stock_manager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Stock extends EntityCommon {

  @Column
  private String name;

  @Column int count;

  @Version
  private Long version;

  @Builder
  public Stock(
      String name,
      int count) {
    this.name = name;
    this.count = count;
  }

  public void decreaseCount(int count) {
    this.count -= count;
  }
}
