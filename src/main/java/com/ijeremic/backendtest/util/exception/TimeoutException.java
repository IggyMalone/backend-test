package com.ijeremic.backendtest.util.exception;

import javax.ws.rs.ServiceUnavailableException;

/**
 * Created by Iggy on 28-Jan-2020
 */
public class TimeoutException extends ServiceUnavailableException
{
  public TimeoutException(String message)
  {
    super(message);
  }
}
