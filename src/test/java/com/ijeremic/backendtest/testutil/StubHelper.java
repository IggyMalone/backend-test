package com.ijeremic.backendtest.testutil;

import com.ijeremic.backendtest.model.Account;
import com.ijeremic.backendtest.model.AccountHolder;
import com.ijeremic.backendtest.model.AccountHolder.Status;
import com.ijeremic.backendtest.model.AccountTransaction;
import com.ijeremic.backendtest.model.enumeration.Currency;
import java.math.BigDecimal;

/**
 * Created by Iggy on 25-Jan-2020
 */
public class StubHelper
{
  public static final String ACCOUNT_NUMBER_1 = "1000";
  public static final String ACCOUNT_NUMBER_2 = "1001";
  public static final String FICTIONAL_ACCOUNT_NUMBER = "3";

  public static Account stubAccount1()
  {
    return stubAccount1(false);
  }

  public static Account stubAccount1(boolean withHolder)
  {
    Account account = new Account();
    account.setAccountNumber(ACCOUNT_NUMBER_1);
    account.setBalance(BigDecimal.TEN);
    account.setCurrency(Currency.EUR);
    if (withHolder)
    {
      account.setAccountHolder(stubAccountHolder1());
    }

    return account;
  }

  public static Account stubAccount2()
  {
    return stubAccount2(false);
  }

  public static Account stubAccount2(boolean withHolder)
  {
    Account account = new Account();
    account.setAccountNumber(ACCOUNT_NUMBER_2);
    account.setBalance(BigDecimal.ONE);
    account.setCurrency(Currency.EUR);
    if (withHolder)
    {
      account.setAccountHolder(stubAccountHolder2());
    }

    return account;
  }

  public static AccountHolder stubAccountHolder1()
  {
    AccountHolder holder = new AccountHolder();
    holder.setId(1L);
    holder.setName("Igor");
    holder.setAddress("Somewhere");
    holder.setStatus(Status.ACTIVE);

    return holder;
  }

  public static AccountHolder stubAccountHolder2()
  {
    AccountHolder holder = new AccountHolder();
    holder.setId(2L);
    holder.setName("Rogi");
    holder.setAddress("Anywhere");
    holder.setStatus(Status.ACTIVE);

    return holder;
  }

  public static AccountTransaction stubAccountTransaction(Account account, BigDecimal amount, Currency currency)
  {
    AccountTransaction at = new AccountTransaction();
    at.setAccount(account);
    at.setAmount(amount);
    at.setCurrency(currency);

    return at;

  }
}
