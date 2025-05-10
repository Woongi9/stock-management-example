package com.example.stock_manager.enums;

import lombok.Getter;

@Getter
public enum Status {
  USE(""),
  DELETED("");

  private final String value;

  Status(String value) {
    this.value = value;
  }
}
