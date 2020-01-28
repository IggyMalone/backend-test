package com.ijeremic.backendtest.rest.exception;

import com.ijeremic.backendtest.rest.exception.dto.ErrorResponse;
import com.ijeremic.backendtest.util.exception.TimeoutException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Created by Iggy on 28-Jan-2020
 */
@Provider
public class TimeoutResponseMapper implements ExceptionMapper<TimeoutException>
{
  public Response toResponse(TimeoutException e)
  {
    return Response.status(Status.SERVICE_UNAVAILABLE).entity(new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE_503, e.getMessage())).type(MediaType.APPLICATION_JSON).build();
  }
}
