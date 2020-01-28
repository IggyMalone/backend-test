package com.ijeremic.backendtest.rest.exception;

import com.ijeremic.backendtest.rest.exception.dto.ErrorResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Created by Iggy on 25-Jan-2020
 */
@Provider
public class BadRequestResponseMapper implements ExceptionMapper<BadRequestException>
{
  public Response toResponse(BadRequestException e)
  {
    return Response.status(Status.BAD_REQUEST).entity(new ErrorResponse(HttpStatus.BAD_REQUEST_400, e.getMessage())).type(MediaType.APPLICATION_JSON).build();
  }
}
