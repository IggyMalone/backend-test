package com.ijeremic.backendtest.model;

import com.ijeremic.backendtest.model.enumeration.Currency;
import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Iggy on 18-Jan-2020.
 */
@Data
@NoArgsConstructor
@Entity
@Table
public class AccountTransaction
{
  public enum TransactionType
  {
    DEPOSIT, CHARGE
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  Long id;

  @ManyToOne(fetch= FetchType.LAZY)
  @JoinColumn(name="account_id", nullable = false)
  Account account;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type")
  TransactionType transactionType;

  @Column(name = "amount")
  BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "currency")
  Currency currency;

  @Column(name = "created")
  Instant created;

  @Column(name = "updated")
  Instant updated;

  @PrePersist
  protected void onCreate()
  {
    created = Instant.now();
  }

  @PreUpdate
  protected void onUpdate()
  {
    updated = Instant.now();
  }
}
