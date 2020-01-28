package com.ijeremic.backendtest.util.exception;

/**
 * Created by Iggy on 27-Jan-2020
 */
public class CommonMessages
{
  public static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found";
  public static final String INSUFFICIENT_FUNDS_MESSAGE = "Insufficient funds in account";
  public static final String INVALID_AMOUNT_MESSAGE = "Transactions can only be made with a positive amount";
  public static final String SAME_ACCOUNT_MESSAGE = "Payer and payee accounts must differ";
  public static final String SERVER_BUSY = "Server is busy, please try again later";

  private CommonMessages() {}
}
