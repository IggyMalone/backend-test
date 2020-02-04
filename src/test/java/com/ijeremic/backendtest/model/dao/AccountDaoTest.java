package com.ijeremic.backendtest.model.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ijeremic.backendtest.model.Account;
import com.ijeremic.backendtest.testutil.StubHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Created by Iggy on 26-Jan-2020
 */
@ExtendWith(MockitoExtension.class)
public class AccountDaoTest
{
  AccountDao dao;
  Map<String, Object> properties;

  @Mock
  static EntityManagerFactory mockEMF;

  @Mock
  static EntityManager entityManager;

  @BeforeEach
  void init()
  {
    dao = new AccountDao();
    GenericDao.emf = mockEMF;
    dao.entityManager = entityManager;

    properties = new HashMap<>();
    properties.put("javax.persistence.lock.timeout", AccountDao.LOCK_TIMEOUT);
  }

  @AfterAll
  static void cleanup()
  {
    GenericDao.emf = null;
  }

  @DisplayName("Regular test getAccountAndLock()")
  @Test
  void getAccountAndLockTest() throws Exception
  {
    Mockito.lenient().when(entityManager.find(Account.class, StubHelper.ACCOUNT_NUMBER_1, LockModeType.PESSIMISTIC_WRITE, properties))
        .thenReturn(new Account());
    Optional<Account> result = dao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1);

    assertTrue(result.isPresent());
  }

  @DisplayName("Account not found test getAccountAndLock()")
  @Test
  void getAccountAndLockAccountNotFoundTest() throws Exception
  {
    Mockito.lenient().when(entityManager.find(Account.class, StubHelper.ACCOUNT_NUMBER_1, LockModeType.PESSIMISTIC_WRITE, properties))
        .thenReturn(null);
    Optional<Account> result = dao.getAccountAndLock(StubHelper.ACCOUNT_NUMBER_1);

    assertFalse(result.isPresent());
  }
}
