package com.ijeremic.backendtest.model.dao;

import com.ijeremic.backendtest.model.Account;
import com.ijeremic.backendtest.util.exception.CommonMessages;
import com.ijeremic.backendtest.util.exception.TimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.LockModeType;

/**
 * Created by Iggy on 19-Jan-2020
 */
@Singleton
public class AccountDao extends GenericDao<Account, String>
{
  @Inject
  public AccountDao(){}

  public Optional<Account> getAccountAndLock(String accountNumber) throws TimeoutException
  {
    try
    {
      Map<String, Object> properties = new HashMap<>();
      properties.put("javax.persistence.lock.timeout", 3000);

      return Optional.ofNullable(entityManager.find(Account.class, accountNumber, LockModeType.PESSIMISTIC_WRITE, properties));
    }
    catch (Exception e)
    {
      throw new TimeoutException(CommonMessages.SERVER_BUSY);
    }
  }
}
