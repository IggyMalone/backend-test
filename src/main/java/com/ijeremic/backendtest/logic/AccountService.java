package com.ijeremic.backendtest.logic;

import com.ijeremic.backendtest.model.Account;
import com.ijeremic.backendtest.model.AccountTransaction;
import com.ijeremic.backendtest.model.enumeration.Currency;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Iggy on 19-Jan-2020
 */
public interface AccountService
{
  Account getAccountData(String accountNumber) throws Exception;

  AccountTransaction depositToAccount(String accountNumber, BigDecimal amount, Currency currency) throws Exception;

  AccountTransaction chargeAccount(String accountNumber, BigDecimal amount, Currency currency) throws Exception;

  List<AccountTransaction> transferBetweenAccounts(String payerAccountNumber, String payeeAccountNumber, BigDecimal amount, Currency currency)
      throws Exception;

}
