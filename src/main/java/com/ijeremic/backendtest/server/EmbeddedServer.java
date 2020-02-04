package com.ijeremic.backendtest.server;

import com.ijeremic.backendtest.util.JerseyInjectionBinder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
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
    ResourceConfig config = new ResourceConfig();
    config.packages("com.ijeremic.backendtest.rest");
    config.register(new JerseyInjectionBinder());
    ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
    ServletContextHandler contextHandler = new ServletContextHandler(server, "/");
    contextHandler.addServlet(jerseyServlet, "/*");

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
