package com.ijeremic.backendtest.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Created by Iggy on 26-Jan-2020
 */
public class AccountTest
{
  @DisplayName("Test account onCreate()")
  @Test
  void onCreateTest()
  {
    Account account = new Account();
    assertNull(account.getCreated());
    account.onCreate();
    assertNotNull(account.getCreated());
  }

  @DisplayName("Test account onUpdate()")
  @Test
  void onUpdateTest()
  {
    Account account = new Account();
    assertNull(account.getUpdated());
    account.onUpdate();
    assertNotNull(account.getUpdated());
  }
}
