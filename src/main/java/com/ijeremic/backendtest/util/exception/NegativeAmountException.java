package com.ijeremic.backendtest.util.exception;

import javax.ws.rs.BadRequestException;

/**
 * Created by Iggy on 25-Jan-2020
 */
public class NegativeAmountException extends BadRequestException
{
  public NegativeAmountException(String message)
  {
    super(message);
  }
}
