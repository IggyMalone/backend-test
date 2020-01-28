package com.ijeremic.backendtest.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Created by Iggy on 18-Jan-2020.
 */
@Data
@NoArgsConstructor
@Entity
@Table
public class AccountHolder implements Serializable
{
  private static final long serialVersionUID = -7139276970418835930L;

  public enum Status
  {
    ACTIVE, RESTRICTED, BANNED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private Long id;

  @ToString.Exclude
  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "accountHolder")
  private Set<Account> account;

  @Column(name = "name")
  private String name;

  @Column(name = "address")
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private Status status;

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
