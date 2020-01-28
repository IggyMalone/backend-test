package com.ijeremic.backendtest.rest.exception;

import com.ijeremic.backendtest.rest.exception.dto.ErrorResponse;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Created by Iggy on 27-Jan-2020
 */
@Provider
public class NotFoundResponseMapper implements ExceptionMapper<NotFoundException>
{
  public Response toResponse(NotFoundException e)
  {
    return Response.status(Status.NOT_FOUND).entity(new ErrorResponse(HttpStatus.NOT_FOUND_404, e.getMessage())).type(MediaType.APPLICATION_JSON).build();
  }
}
