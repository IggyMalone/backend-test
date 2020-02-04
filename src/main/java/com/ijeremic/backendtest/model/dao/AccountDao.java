package com.ijeremic.backendtest.model.dao;

import com.ijeremic.backendtest.model.Account;
import com.ijeremic.backendtest.util.exception.CommonMessages;
import com.ijeremic.backendtest.util.exception.TimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.persistence.LockModeType;

/**
 * Created by Iggy on 19-Jan-2020
 */
public class AccountDao extends GenericDao<Account, String>
{
  protected static Integer LOCK_TIMEOUT = 500;

  @Inject
  public AccountDao(){}

  public Optional<Account> getAccountAndLock(String accountNumber) throws TimeoutException
  {
    try
    {
      Map<String, Object> properties = new HashMap<>();
      properties.put("javax.persistence.lock.timeout", LOCK_TIMEOUT);

      return Optional.ofNullable(entityManager.find(Account.class, accountNumber, LockModeType.PESSIMISTIC_WRITE, properties));
    }
    catch (Exception e)
    {
      throw new TimeoutException(CommonMessages.SERVER_BUSY);
    }
  }
}
