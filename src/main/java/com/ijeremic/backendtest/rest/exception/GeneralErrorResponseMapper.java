package com.ijeremic.backendtest.rest.exception;

import com.ijeremic.backendtest.rest.exception.dto.ErrorResponse;
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
public class GeneralErrorResponseMapper implements ExceptionMapper<Exception>
{
  public Response toResponse(Exception e)
  {
    e.printStackTrace();
    return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR_500, "Unexpected server error")).type(MediaType.APPLICATION_JSON).build();
  }
}
