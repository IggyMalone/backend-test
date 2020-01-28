package com.ijeremic.backendtest.rest.dto;

import com.ijeremic.backendtest.model.AccountTransaction;
import com.ijeremic.backendtest.model.AccountTransaction.TransactionType;
import com.ijeremic.backendtest.model.enumeration.Currency;
import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Iggy on 21-Jan-2020
 */
@XmlRootElement
@Data
@NoArgsConstructor
public class AccountTransactionDto
{
  Long accountTransactionId;
  TransactionType transactionType;
  BigDecimal amount;
  Currency currency;

  public AccountTransactionDto(AccountTransaction accountTransaction)
  {
    this.accountTransactionId = accountTransaction.getId();
    this.transactionType = accountTransaction.getTransactionType();
    this.amount = accountTransaction.getAmount();
    this.currency = accountTransaction.getCurrency();
  }
}
