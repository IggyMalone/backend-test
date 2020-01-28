package com.ijeremic.backendtest.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ijeremic.backendtest.model.Account;
import com.ijeremic.backendtest.model.AccountTransaction;
import com.ijeremic.backendtest.model.dao.AccountDao;
import com.ijeremic.backendtest.model.dao.AccountTransactionDao;
import com.ijeremic.backendtest.model.enumeration.Currency;
import com.ijeremic.backendtest.testutil.StubHelper;
import com.ijeremic.backendtest.util.exception.CommonMessages;
import com.ijeremic.backendtest.util.exception.InsufficientFundsException;
import com.ijeremic.backendtest.util.exception.NegativeAmountException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Created by Iggy on 25-Jan-2020
 */
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest
{
  AccountServiceImpl service = new AccountServiceImpl();

  @Mock
  AccountDao accountDao;

  @Mock
  AccountTransactionDao accountTransactionDao;

  @BeforeEach
  void init()
  {
    service.accountDao = accountDao;
    service.accountTransactionDao = accountTransactionDao;
  }

  @DisplayName("Test new AccountService()")
  @Test
  void serviceCreationTest()
  {
    service = new AccountServiceImpl();
    assertNotNull(service.accountDao);
    assertNotNull(service.accountTransactionDao);
  }

  @DisplayName("Regular test getAccountData()")
  @Test
  void getAccountDataTest() throws Exception
  {
    Mockito.lenient().when(accountDao.getById(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(StubHelper.stubAccount1()));

    Account account = service.getAccountData(StubHelper.ACCOUNT_NUMBER_1);
    assertNotNull(account);
  }

  @DisplayName("Missing account test getAccountData()")
  @Test
  void getAccountDataMissingTest()
  {
    Mockito.lenient().when(accountDao.getById(StubHelper.FICTIONAL_ACCOUNT_NUMBER)).thenReturn(Optional.empty());

    NotFoundException thrown = assertThrows(NotFoundException.class,
            () ->service.getAccountData(StubHelper.FICTIONAL_ACCOUNT_NUMBER),
            CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);

    assertEquals(thrown.getMessage(), CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);
  }

  @DisplayName("Regular test depositToAccount()")
  @Test
  void depositToAccountTest() throws Exception
  {
    BigDecimal depositAmount = BigDecimal.ONE;
    Account account1 = StubHelper.stubAccount1();
    Account accountWithUpdate = StubHelper.stubAccount1();
    accountWithUpdate.setBalance(accountWithUpdate.getBalance().add(depositAmount));
    AccountTransaction at1 = StubHelper.stubAccountTransaction(account1, depositAmount, Currency.EUR);

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_1, accountWithUpdate)).thenReturn(Optional.of(accountWithUpdate));
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenReturn(at1);

    AccountTransaction resultAt = service.depositToAccount(StubHelper.ACCOUNT_NUMBER_1, depositAmount, Currency.EUR);
    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).commitTransaction();

    assertEquals(at1, resultAt);
  }

  @DisplayName("Missing account test depositToAccount()")
  @Test
  void depositToAccountMissingAccountTest() throws Exception
  {
    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.FICTIONAL_ACCOUNT_NUMBER)).thenReturn(Optional.ofNullable(null));

    NotFoundException thrown = assertThrows(NotFoundException.class,
        () ->service.depositToAccount(StubHelper.FICTIONAL_ACCOUNT_NUMBER, BigDecimal.ONE, Currency.EUR),
        CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();
    assertEquals(thrown.getMessage(), CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);
  }

  @DisplayName("Zero amount test depositToAccount()")
  @Test
  void depositToAccountZeroAmountTest()
  {
    NegativeAmountException thrown = assertThrows(NegativeAmountException.class,
        () ->service.depositToAccount(StubHelper.ACCOUNT_NUMBER_1, BigDecimal.ZERO, Currency.EUR),
        CommonMessages.INVALID_AMOUNT_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(0)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();
    assertEquals(thrown.getMessage(), CommonMessages.INVALID_AMOUNT_MESSAGE);
  }

  @DisplayName("Negative amount test depositToAccount()")
  @Test
  void depositToAccountNegativeAmountTest()
  {
    NegativeAmountException thrown = assertThrows(NegativeAmountException.class,
        () ->service.depositToAccount(StubHelper.ACCOUNT_NUMBER_1, BigDecimal.ONE.negate(), Currency.EUR),
        CommonMessages.INVALID_AMOUNT_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(0)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();
    assertEquals(thrown.getMessage(), CommonMessages.INVALID_AMOUNT_MESSAGE);
  }

  @DisplayName("Account update failure test depositToAccount()")
  @Test
  void depositToAccountUpdateFailureTest() throws Exception
  {
    String customExceptionMessage = "This is an exception";
    BigDecimal depositAmount = BigDecimal.ONE;

    Account account1 = StubHelper.stubAccount1();
    Account accountWithUpdate = StubHelper.stubAccount1();
    accountWithUpdate.setBalance(accountWithUpdate.getBalance().add(depositAmount));

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_1, accountWithUpdate))
        .thenThrow(new RuntimeException(customExceptionMessage));

    Exception thrown = assertThrows(Exception.class,
        () ->service.depositToAccount(StubHelper.ACCOUNT_NUMBER_1, depositAmount, Currency.EUR),
        customExceptionMessage);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), customExceptionMessage);
  }

  @DisplayName("Account transaction failure test depositToAccount()")
  @Test
  void depositToAccountAccountTransactionFailureTest() throws Exception
  {
    String customExceptionMessage = "This is an exception";

    Account account1 = StubHelper.stubAccount1();

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenThrow(new RuntimeException(customExceptionMessage));

    Exception thrown = assertThrows(Exception.class,
        () ->service.depositToAccount(StubHelper.ACCOUNT_NUMBER_1, BigDecimal.ONE, Currency.EUR),
        CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), customExceptionMessage);
  }

  @DisplayName("Commit failure test depositToAccount()")
  @Test
  void depositToAccountCommitFailureTest() throws Exception
  {
    String customExceptionMessage = "This is an exception";

    BigDecimal depositAmount = BigDecimal.ONE;
    Account account1 = StubHelper.stubAccount1();
    Account accountWithUpdate = StubHelper.stubAccount1();
    accountWithUpdate.setBalance(accountWithUpdate.getBalance().add(depositAmount));
    AccountTransaction at1 = StubHelper.stubAccountTransaction(account1, depositAmount, Currency.EUR);

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_1, accountWithUpdate)).thenReturn(Optional.of(accountWithUpdate));
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenReturn(at1);
    Mockito.lenient().doThrow(new RuntimeException(customExceptionMessage)).when(accountDao).commitTransaction();

    Exception thrown = assertThrows(Exception.class,
        () ->service.depositToAccount(StubHelper.ACCOUNT_NUMBER_1, depositAmount, Currency.EUR),
        customExceptionMessage);
    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).commitTransaction();

    assertEquals(thrown.getMessage(), customExceptionMessage);
  }

  @DisplayName("Regular test chargeAccount()")
  @Test
  void chargeAccountTest() throws Exception
  {
    BigDecimal chargeAmount = BigDecimal.ONE;
    Account account1 = StubHelper.stubAccount1();
    Account accountWithUpdate = StubHelper.stubAccount1();
    accountWithUpdate.setBalance(accountWithUpdate.getBalance().subtract(chargeAmount));
    AccountTransaction at1 = StubHelper.stubAccountTransaction(account1, chargeAmount, Currency.EUR);

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_1, accountWithUpdate)).thenReturn(Optional.of(accountWithUpdate));
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenReturn(at1);

    AccountTransaction resultAt = service.chargeAccount(StubHelper.ACCOUNT_NUMBER_1, chargeAmount, Currency.EUR);
    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).commitTransaction();

    assertEquals(at1, resultAt);
  }

  @DisplayName("Zero amount test chargeAccount()")
  @Test
  void chargeAccountZeroAmountTest()
  {
    NegativeAmountException thrown = assertThrows(NegativeAmountException.class,
        () ->service.chargeAccount(StubHelper.ACCOUNT_NUMBER_1, BigDecimal.ZERO, Currency.EUR),
        CommonMessages.INVALID_AMOUNT_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(0)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();
    assertEquals(thrown.getMessage(), CommonMessages.INVALID_AMOUNT_MESSAGE);
  }

  @DisplayName("Negative amount test chargeAccount()")
  @Test
  void chargeAccountNegativeAmountTest()
  {
    NegativeAmountException thrown = assertThrows(NegativeAmountException.class,
        () ->service.chargeAccount(StubHelper.ACCOUNT_NUMBER_1, BigDecimal.ONE.negate(), Currency.EUR),
        CommonMessages.INVALID_AMOUNT_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(0)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();
    assertEquals(thrown.getMessage(), CommonMessages.INVALID_AMOUNT_MESSAGE);
  }

  @DisplayName("Account insufficient funds test chargeAccount()")
  @Test
  void chargeAccountInsufficientFundsTest() throws Exception
  {
    BigDecimal chargeAmount = BigDecimal.ONE;
    Account account1 = StubHelper.stubAccount1();
    account1.setBalance(BigDecimal.ZERO);

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));

    InsufficientFundsException thrown = assertThrows(InsufficientFundsException.class,
        () ->service.chargeAccount(StubHelper.ACCOUNT_NUMBER_1, chargeAmount, Currency.EUR),
        CommonMessages.INSUFFICIENT_FUNDS_MESSAGE);
    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), CommonMessages.INSUFFICIENT_FUNDS_MESSAGE);
  }

  @DisplayName("Missing account test chargeAccount()")
  @Test
  void chargeAccountMissingAccountTest() throws Exception
  {
    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.FICTIONAL_ACCOUNT_NUMBER)).thenReturn(Optional.ofNullable(null));

    NotFoundException thrown = assertThrows(NotFoundException.class,
        () ->service.chargeAccount(StubHelper.FICTIONAL_ACCOUNT_NUMBER, BigDecimal.ONE, Currency.EUR),
        CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);
  }

  @DisplayName("Account update failure test chargeAccount()")
  @Test
  void chargeAccountUpdateFailureTest() throws Exception
  {
    String customExceptionMessage = "This is an exception";
    BigDecimal chargeAmount = BigDecimal.ONE;

    Account account1 = StubHelper.stubAccount1();
    Account accountWithUpdate = StubHelper.stubAccount1();
    accountWithUpdate.setBalance(accountWithUpdate.getBalance().subtract(chargeAmount));

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_1, accountWithUpdate))
        .thenThrow(new RuntimeException(customExceptionMessage));

    Exception thrown = assertThrows(Exception.class,
        () ->service.chargeAccount(StubHelper.ACCOUNT_NUMBER_1, chargeAmount, Currency.EUR),
        customExceptionMessage);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), customExceptionMessage);
  }

  @DisplayName("Account transaction failure test chargeAccount()")
  @Test
  void chargeAccountAccountTransactionFailureTest() throws Exception
  {
    String customExceptionMessage = "This is an exception";

    Account account1 = StubHelper.stubAccount1();

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenThrow(new RuntimeException(customExceptionMessage));

    Exception thrown = assertThrows(Exception.class,
        () ->service.chargeAccount(StubHelper.ACCOUNT_NUMBER_1, BigDecimal.ONE, Currency.EUR),
        CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), customExceptionMessage);
  }

  @DisplayName("Commit failure test chargeAccount()")
  @Test
  void chargeAccountCommitFailureTest() throws Exception
  {
    String customExceptionMessage = "This is an exception";

    BigDecimal chargeAmount = BigDecimal.ONE;
    Account account1 = StubHelper.stubAccount1();
    Account accountWithUpdate = StubHelper.stubAccount1();
    accountWithUpdate.setBalance(accountWithUpdate.getBalance().subtract(chargeAmount));
    AccountTransaction at1 = StubHelper.stubAccountTransaction(account1, chargeAmount, Currency.EUR);

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_1, accountWithUpdate)).thenReturn(Optional.of(accountWithUpdate));
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenReturn(at1);
    Mockito.lenient().doThrow(new RuntimeException(customExceptionMessage)).when(accountDao).commitTransaction();

    Exception thrown = assertThrows(Exception.class,
        () ->service.chargeAccount(StubHelper.ACCOUNT_NUMBER_1, chargeAmount, Currency.EUR),
        customExceptionMessage);
    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).commitTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();

    assertEquals(thrown.getMessage(), customExceptionMessage);
  }

  @DisplayName("Regular test transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsTest() throws Exception
  {
    BigDecimal amount = BigDecimal.ONE;
    Account account1 = StubHelper.stubAccount1();
    Account account2 = StubHelper.stubAccount2();
    Account account1WithUpdate = StubHelper.stubAccount1();
    account1WithUpdate.setBalance(account1WithUpdate.getBalance().add(amount));
    Account account2WithUpdate = StubHelper.stubAccount1();
    account2WithUpdate.setBalance(account2WithUpdate.getBalance().subtract(amount));
    AccountTransaction at1 = StubHelper.stubAccountTransaction(account1, amount, Currency.EUR);
    AccountTransaction at2 = StubHelper.stubAccountTransaction(account2, amount, Currency.EUR);

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_2)).thenReturn(Optional.of(account2));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_1, account1WithUpdate)).thenReturn(Optional.of(account1WithUpdate));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_2, account2WithUpdate)).thenReturn(Optional.of(account2WithUpdate));
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenReturn(at1);
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenReturn(at2);

    List<AccountTransaction> resultATList = service.transferBetweenAccounts(StubHelper.ACCOUNT_NUMBER_1, StubHelper.ACCOUNT_NUMBER_2, amount, Currency.EUR);
    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).commitTransaction();

    assertEquals(2, resultATList.size());
  }

  @DisplayName("Zero amount test transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsZeroAmountTest()
  {
    NegativeAmountException thrown = assertThrows(NegativeAmountException.class,
        () ->service.transferBetweenAccounts(StubHelper.ACCOUNT_NUMBER_1, StubHelper.ACCOUNT_NUMBER_2, BigDecimal.ZERO, Currency.EUR),
        CommonMessages.INVALID_AMOUNT_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(0)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();
    assertEquals(thrown.getMessage(), CommonMessages.INVALID_AMOUNT_MESSAGE);
  }

  @DisplayName("Negative amount test transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsNegativeAmountTest()
  {
    NegativeAmountException thrown = assertThrows(NegativeAmountException.class,
        () ->service.transferBetweenAccounts(StubHelper.ACCOUNT_NUMBER_1, StubHelper.ACCOUNT_NUMBER_2, BigDecimal.ONE.negate(), Currency.EUR),
        CommonMessages.INVALID_AMOUNT_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(0)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();
    assertEquals(thrown.getMessage(), CommonMessages.INVALID_AMOUNT_MESSAGE);
  }

  @DisplayName("Missing payer account test transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsMissingPayerAccountTest() throws Exception
  {
    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.FICTIONAL_ACCOUNT_NUMBER)).thenReturn(Optional.ofNullable(null));

    NotFoundException thrown = assertThrows(NotFoundException.class,
        () ->service.transferBetweenAccounts(StubHelper.FICTIONAL_ACCOUNT_NUMBER, StubHelper.ACCOUNT_NUMBER_2, BigDecimal.ONE, Currency.EUR),
        CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);
  }

  @DisplayName("Missing payee account test transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsMissingPayeeAccountTest() throws Exception
  {
    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.FICTIONAL_ACCOUNT_NUMBER)).thenReturn(null);

    NotFoundException thrown = assertThrows(NotFoundException.class,
        () ->service.transferBetweenAccounts(StubHelper.ACCOUNT_NUMBER_1, StubHelper.FICTIONAL_ACCOUNT_NUMBER, BigDecimal.ONE, Currency.EUR),
        CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1);
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), CommonMessages.ACCOUNT_NOT_FOUND_MESSAGE);
  }

  @DisplayName("Same payer and payee account test transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsSameAccountTest()
  {
    BadRequestException thrown = assertThrows(BadRequestException.class,
        () ->service.transferBetweenAccounts(StubHelper.FICTIONAL_ACCOUNT_NUMBER, StubHelper.FICTIONAL_ACCOUNT_NUMBER, BigDecimal.ONE, Currency.EUR),
        CommonMessages.SAME_ACCOUNT_MESSAGE);

    assertEquals(thrown.getMessage(), CommonMessages.SAME_ACCOUNT_MESSAGE);
  }

  @DisplayName("Payer account update failure test transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsAccountUpdate1FailureTest() throws Exception
  {
    String customExceptionMessage = "This is an exception";

    BigDecimal amount = BigDecimal.ONE;
    Account account1 = StubHelper.stubAccount1();
    Account account2 = StubHelper.stubAccount2();
    Account account1WithUpdate = StubHelper.stubAccount1();
    account1WithUpdate.setBalance(account1WithUpdate.getBalance().subtract(amount));
    Account account2WithUpdate = StubHelper.stubAccount1();
    account2WithUpdate.setBalance(account2WithUpdate.getBalance().add(amount));

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_2)).thenReturn(Optional.of(account2));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_1, account1WithUpdate))
        .thenThrow(new RuntimeException(customExceptionMessage));

    Exception thrown = assertThrows(Exception.class,
        () ->service.transferBetweenAccounts(StubHelper.ACCOUNT_NUMBER_1, StubHelper.ACCOUNT_NUMBER_2, amount, Currency.EUR),
        customExceptionMessage);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), customExceptionMessage);
  }

  @DisplayName("Payee account update failure test transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsAccountUpdate2FailureTest() throws Exception
  {
    String customExceptionMessage = "This is an exception";

    BigDecimal amount = BigDecimal.ONE;
    Account account1 = StubHelper.stubAccount1();
    Account account2 = StubHelper.stubAccount2();
    Account account1WithUpdate = StubHelper.stubAccount1();
    account1WithUpdate.setBalance(account1WithUpdate.getBalance().subtract(amount));
    Account account2WithUpdate = StubHelper.stubAccount2();
    account2WithUpdate.setBalance(account2WithUpdate.getBalance().add(amount));

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_2)).thenReturn(Optional.of(account2));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_1, account1WithUpdate)).thenReturn(Optional.of(account1WithUpdate));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_2, account2WithUpdate))
        .thenThrow(new RuntimeException(customExceptionMessage));

    Exception thrown = assertThrows(Exception.class,
        () ->service.transferBetweenAccounts(StubHelper.ACCOUNT_NUMBER_1, StubHelper.ACCOUNT_NUMBER_2, amount, Currency.EUR),
        customExceptionMessage);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).update(StubHelper.ACCOUNT_NUMBER_1, account1WithUpdate);
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), customExceptionMessage);
  }

  @DisplayName("Account transaction failure test transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsAccountTransactionFailureTest() throws Exception
  {
    String customExceptionMessage = "This is an exception";

    BigDecimal amount = BigDecimal.ONE;
    Account account1 = StubHelper.stubAccount1();
    Account account2 = StubHelper.stubAccount2();
    Account account1WithUpdate = StubHelper.stubAccount1();
    account1WithUpdate.setBalance(account1WithUpdate.getBalance().subtract(amount));
    Account account2WithUpdate = StubHelper.stubAccount2();
    account2WithUpdate.setBalance(account2WithUpdate.getBalance().add(amount));

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_2)).thenReturn(Optional.of(account2));
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenThrow(new RuntimeException(customExceptionMessage));

    Exception thrown = assertThrows(Exception.class,
        () ->service.transferBetweenAccounts(StubHelper.ACCOUNT_NUMBER_1, StubHelper.ACCOUNT_NUMBER_2, amount, Currency.EUR),
        customExceptionMessage);

    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).update(StubHelper.ACCOUNT_NUMBER_1, account1WithUpdate);
    Mockito.verify(accountDao, Mockito.times(1)).update(StubHelper.ACCOUNT_NUMBER_2, account2WithUpdate);
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();
    Mockito.verify(accountDao, Mockito.times(0)).commitTransaction();

    assertEquals(thrown.getMessage(), customExceptionMessage);
  }

  @DisplayName("Commit failure test transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsCommitFailureTest() throws Exception
  {
    String customExceptionMessage = "This is an exception";

    BigDecimal amount = BigDecimal.ONE;
    Account account1 = StubHelper.stubAccount1();
    Account account2 = StubHelper.stubAccount2();
    Account account1WithUpdate = StubHelper.stubAccount1();
    account1WithUpdate.setBalance(account1WithUpdate.getBalance().add(amount));
    Account account2WithUpdate = StubHelper.stubAccount2();
    account2WithUpdate.setBalance(account2WithUpdate.getBalance().subtract(amount));
    AccountTransaction at1 = StubHelper.stubAccountTransaction(account1, amount, Currency.EUR);
    AccountTransaction at2 = StubHelper.stubAccountTransaction(account2, amount, Currency.EUR);

    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1)).thenReturn(Optional.of(account1));
    Mockito.lenient().when(accountDao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_2)).thenReturn(Optional.of(account2));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_1, account1WithUpdate)).thenReturn(Optional.of(account1WithUpdate));
    Mockito.lenient().when(accountDao.update(StubHelper.ACCOUNT_NUMBER_2, account2WithUpdate)).thenReturn(Optional.of(account2WithUpdate));
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenReturn(at1);
    Mockito.lenient().when(accountTransactionDao.persist(ArgumentMatchers.any(AccountTransaction.class)))
        .thenReturn(at2);

    Mockito.lenient().doThrow(new RuntimeException(customExceptionMessage)).when(accountDao).commitTransaction();

    Exception thrown = assertThrows(Exception.class,
        () ->service.transferBetweenAccounts(StubHelper.ACCOUNT_NUMBER_1, StubHelper.ACCOUNT_NUMBER_2, amount, Currency.EUR),
        customExceptionMessage);
    Mockito.verify(accountDao, Mockito.times(1)).startTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).commitTransaction();
    Mockito.verify(accountDao, Mockito.times(1)).rollbackTransaction();

    assertEquals(thrown.getMessage(), customExceptionMessage);
  }
}
