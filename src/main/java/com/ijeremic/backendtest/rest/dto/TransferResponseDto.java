package com.ijeremic.backendtest.rest.dto;

import com.ijeremic.backendtest.model.AccountTransaction;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Iggy on 21-Jan-2020
 */
@XmlRootElement
@Data
@NoArgsConstructor
public class TransferResponseDto
{
  private AccountDto account;
  private AccountTransactionDto accountTransactionDto;

  public TransferResponseDto(AccountTransaction accountTransaction)
  {
    this.account = new AccountDto(accountTransaction);
    this.accountTransactionDto = new AccountTransactionDto(accountTransaction);
  }
}
