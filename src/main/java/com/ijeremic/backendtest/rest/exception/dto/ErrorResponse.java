package com.ijeremic.backendtest.rest.exception.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Iggy on 25-Jan-2020
 */
@XmlRootElement
public class ErrorResponse
{
  public int status;
  public String message;

  public ErrorResponse() {}

  public ErrorResponse(int status, String message)
  {
    this.status = status;
    this.message = message;
  }
}
