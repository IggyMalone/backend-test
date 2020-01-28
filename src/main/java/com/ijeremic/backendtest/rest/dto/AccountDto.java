package com.ijeremic.backendtest.rest.dto;

import com.ijeremic.backendtest.model.Account;
import com.ijeremic.backendtest.model.AccountTransaction;
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
public class AccountDto
{
  private String accountNumber;
  private BigDecimal balance;
  private BigDecimal reservedBalance;
  private Currency currency;

  public AccountDto(AccountTransaction accountTransaction)
  {
    this.accountNumber = accountTransaction.getAccount().getAccountNumber();
    this.balance = accountTransaction.getAccount().getBalance();
    this.reservedBalance = accountTransaction.getAccount().getReservedBalance();
    this.currency = accountTransaction.getCurrency();
  }

  public static AccountDto fromAccount(Account account)
  {
    AccountDto dto = new AccountDto();
    dto.setAccountNumber(account.getAccountNumber());
    dto.setBalance(account.getBalance());
    dto.setReservedBalance(account.getReservedBalance());
    dto.setCurrency(account.getCurrency());
    return dto;
  }
}
