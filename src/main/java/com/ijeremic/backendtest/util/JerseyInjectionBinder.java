package com.ijeremic.backendtest.util;

import com.ijeremic.backendtest.logic.AccountService;
import com.ijeremic.backendtest.logic.AccountServiceImpl;
import com.ijeremic.backendtest.model.dao.AccountDao;
import com.ijeremic.backendtest.model.dao.AccountTransactionDao;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Created by Iggy on 03-Feb-2020
 */
public class JerseyInjectionBinder extends AbstractBinder
{
  @Override
  protected void configure() {
    bind(AccountServiceImpl.class).to(AccountService.class);
    bind(AccountDao.class).to(AccountDao.class);
    bind(AccountTransactionDao.class).to(AccountTransactionDao.class);
  }
}
