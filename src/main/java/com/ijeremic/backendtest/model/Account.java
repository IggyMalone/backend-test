package com.ijeremic.backendtest.model;

import com.ijeremic.backendtest.model.enumeration.Currency;
import java.math.BigDecimal;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Iggy on 18-Jan-2020.
 */
@Data
@NoArgsConstructor
@Entity
@Table
public class Account
{
  @Id
  @Column(name = "account_number", unique = true, nullable = false)
  @Size(min = 10, max = 10)
  private String accountNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_holder_id", referencedColumnName = "id")
  private AccountHolder accountHolder;

  @Column(name = "balance")
  private BigDecimal balance;

  @Enumerated(EnumType.STRING)
  @Column(name = "currency")
  private Currency currency;

  @Column(name = "reserved_balance")
  private BigDecimal reservedBalance;

  @Column(name = "created")
  private Instant created;

  @Column(name = "updated")
  private Instant updated;

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
