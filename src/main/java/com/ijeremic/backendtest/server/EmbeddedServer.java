package com.ijeremic.backendtest.server;

import com.ijeremic.backendtest.rest.AccountApi;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Created by Iggy on 24-Jan-2020
 */
public class EmbeddedServer
{
  private static final int PORT = 8000;

  private EmbeddedServer()
  {
  }

  public static void main(String[] args)
      throws Exception
  {
    Server server = new Server(PORT);

    ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    contextHandler.setContextPath("/");
    server.setHandler(contextHandler);

    ServletHolder servletHolder = contextHandler.addServlet(ServletContainer.class, "/*");
    servletHolder.setInitOrder(0);
    servletHolder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "com.ijeremic.backendtest.rest");
    servletHolder.setInitParameter("jersey.config.server.provider.classnames", AccountApi.class.getCanonicalName());

    try
    {
      server.start();
      server.join();
    }
    finally
    {

      server.destroy();
    }
  }
}
