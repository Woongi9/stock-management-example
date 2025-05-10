package com.example.stock_manager.domain;

import com.example.stock_manager.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.Date;
import lombok.Getter;

@Getter
@MappedSuperclass
public class EntityCommon {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column
  private Long id;

  @Column
  private Date createdDate;

  @Column
  private Date updatedDate;

  @Column
  @Enumerated(EnumType.STRING)
  private Status status;

  @PrePersist
  protected void onCreate() {
    this.createdDate = new Date();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedDate = new Date();
  }
}
