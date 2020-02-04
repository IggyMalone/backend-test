package com.ijeremic.backendtest.logic;

import com.ijeremic.backendtest.model.Account;
import com.ijeremic.backendtest.model.AccountTransaction;
import com.ijeremic.backendtest.model.AccountTransaction.TransactionType;
import com.ijeremic.backendtest.model.dao.AccountDao;
import com.ijeremic.backendtest.model.dao.AccountTransactionDao;
import com.ijeremic.backendtest.model.enumeration.Currency;
import com.ijeremic.backendtest.util.exception.CommonMessages;
import com.ijeremic.backendtest.util.exception.InsufficientFundsException;
import com.ijeremic.backendtest.util.exception.NegativeAmountException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

/**
 * Created by Iggy on 27-Jan-2020
 */
public class AccountServiceImpl implements AccountService
{
  protected AccountDao accountDao;
  protected AccountTransactionDao accountTransactionDao;

  @Inject
  public AccountServiceImpl(AccountDao accountDao, AccountTransactionDao accountTransactionDao)
  {
    this.accountDao = accountDao;
    this.accountTransactionDao = accountTransactionDao;
  }

  public Account getAccountData(String accountNumber) throws Exception
  {
    return accountDao.getById(accountNumber).orElseThrow(() -> new NotFoundException(CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE));
  }

  public AccountTransaction depositToAccount(String accountNumber, BigDecimal amount, Currency currency)
      throws Exception
  {
    checkAmount(amount);

    try
    {
      accountDao.startTransaction();
      Optional<Account> account = accountDao.getAccountAndLock(accountNumber);
      checkAccount(account);
      accountUpdateAddBalance(account.get(), amount);
      AccountTransaction at = createAccountTransaction(account.get(), amount, currency, TransactionType.DEPOSIT, Instant.now());
      AccountTransaction newAT = accountTransactionDao.persist(at);

      accountDao.commitTransaction();
      return newAT;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw e;
    }
    finally
    {
      accountDao.rollbackTransaction();
    }
  }

  public AccountTransaction chargeAccount(String accountNumber, BigDecimal amount, Currency currency)
      throws Exception
  {
    checkAmount(amount);

    try
    {
      accountDao.startTransaction();
      Optional<Account> account = accountDao.getAccountAndLock(accountNumber);
      checkAccount(account);
      accountUpdateDeductBalance(account.get(), amount);
      AccountTransaction at = createAccountTransaction(account.get(), amount, currency, TransactionType.CHARGE, Instant.now());
      AccountTransaction newAt = accountTransactionDao.persist(at);
      accountDao.commitTransaction();
      return newAt;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw e;
    }
    finally
    {
      accountDao.rollbackTransaction();
    }
  }

  public List<AccountTransaction> transferBetweenAccounts(String payerAccountNumber, String payeeAccountNumber, BigDecimal amount, Currency currency)
      throws Exception
  {
    if (payerAccountNumber.equals(payeeAccountNumber))
    {
      throw new BadRequestException(CommonMessages.SAME_ACCOUNT_MESSAGE);
    }

    checkAmount(amount);

    try
    {
      accountDao.startTransaction();
      Instant now = Instant.now();
      Optional<Account> payerAccount = accountDao.getAccountAndLock(payerAccountNumber);
      checkAccount(payerAccount);
      Optional<Account> payeeAccount = accountDao.getAccountAndLock(payeeAccountNumber);
      checkAccount(payeeAccount);

      accountUpdateDeductBalance(payerAccount.get(), amount);
      accountUpdateAddBalance(payeeAccount.get(), amount);

      List<AccountTransaction> accountTransactions = new ArrayList<>();
      AccountTransaction chargeAT = createAccountTransaction(payerAccount.get(), amount, currency, TransactionType.DEPOSIT, now);
      AccountTransaction depositAT = createAccountTransaction(payeeAccount.get(), amount, currency, TransactionType.CHARGE, now);
      accountTransactions.add(accountTransactionDao.persist(chargeAT));
      accountTransactions.add(accountTransactionDao.persist(depositAT));

      accountDao.commitTransaction();
      return accountTransactions;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw e;
    }
    finally
    {
      accountDao.rollbackTransaction();
    }
  }

  private AccountTransaction createAccountTransaction(Account account, BigDecimal amount, Currency currency, TransactionType transactionType, Instant created)
  {
    AccountTransaction at = new AccountTransaction();
    at.setAccount(account);
    at.setAmount(amount);
    at.setCurrency(currency);
    at.setTransactionType(transactionType);
    at.setCreated(created);

    return at;
  }

  private void accountUpdateAddBalance(Account account, BigDecimal amount) throws Exception
  {
    BigDecimal newBalance = account.getBalance().add(amount);
    account.setBalance(newBalance);
    accountDao.update(account.getAccountNumber(), account);
  }

  private void accountUpdateDeductBalance(Account account, BigDecimal amount) throws Exception
  {
    BigDecimal newBalance = account.getBalance().subtract(amount);
    if (newBalance.compareTo(BigDecimal.ZERO) < 0)
    {
      throw new InsufficientFundsException(CommonMessages.INSUFFICIENT_FUNDS_MESSAGE);
    }
    account.setBalance(newBalance);
    accountDao.update(account.getAccountNumber(), account);
  }

  private void checkAmount(BigDecimal amount) throws BadRequestException
  {
    if (amount.compareTo(BigDecimal.ZERO) <= 0)
    {
      throw new NegativeAmountException(CommonMessages.INVALID_AMOUNT_MESSAGE);
    }
  }

  private void checkAccount(Optional<Account> account) throws NotFoundException
  {
    if (!account.isPresent())
    {
      throw new NotFoundException(CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);
    }
  }
}
