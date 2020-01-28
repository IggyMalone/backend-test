package com.ijeremic.backendtest.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ijeremic.backendtest.model.enumeration.Currency;
import com.ijeremic.backendtest.rest.dto.AccountDto;
import com.ijeremic.backendtest.rest.dto.AccountsTransferDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Created by Iggy on 27-Jan-2020
 */
public class AccountApiConcurrencyTest
{
  static Server server;
  Random random = new Random();

  @BeforeAll
  public static void setup()
      throws Exception
  {
    RestAssured.port = Integer.valueOf(8000);
    RestAssured.basePath = "/";
    RestAssured.baseURI = "http://localhost";

    server = new Server(8000);
    ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    contextHandler.setContextPath("/");
    server.setHandler(contextHandler);

    ServletHolder servletHolder = contextHandler.addServlet(ServletContainer.class, "/*");
    servletHolder.setInitOrder(0);
    servletHolder.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "com.ijeremic.backendtest.rest");
    servletHolder.setInitParameter("jersey.config.server.provider.classnames", AccountApi.class.getCanonicalName());

    server.start();
  }

  @AfterAll
  public static void teardown()
      throws Exception
  {
    server.stop();
  }

  @DisplayName("Transfer concurrency test")
  @Test
  void accountDataTest() throws Exception
  {
    AccountDto payerAccountDto = getAccount("1000001000");
    AccountDto payeeAccountDto = getAccount("1000001001");

    int threads = 5;
    int spins = 100;
    Collection<Future<Integer>> futures = new ArrayList<>(threads);
    List<Integer> returnCodes = new ArrayList<>();
    ExecutorService executorService = Executors.newFixedThreadPool(threads);

    for (int i = 0; i < 1000; ++i)
    {
      for (int t = 0; t < threads; ++t)
      {
        futures.add(executorService.submit(() -> doTransfer()));
      }
      for(Future<Integer> f : futures)
      {
        returnCodes.add(f.get());
      }
      TimeUnit.MILLISECONDS.sleep(200);
      futures.clear();
    }

    AccountDto payerAccountDtoAfter = getAccount("1000001000");
    AccountDto payeeAccountDtoAfter = getAccount("1000001001");

    //all threads made a request
    assertEquals(threads * spins, returnCodes.size());

    //all transactions were fully finished, resulting in same balance differences between two accounts
    assertEquals(payerAccountDto.getBalance().subtract(payerAccountDtoAfter.getBalance()).abs(),
        payeeAccountDto.getBalance().subtract(payeeAccountDtoAfter.getBalance()).abs()
    );
  }

  private int doTransfer()
  {
    String accountNumber1 = "1000001000";
    String accountNumber2 = "1000001001";
    int rand = random.nextInt(10);

    BigDecimal depositAmount = BigDecimal.ONE;
    AccountsTransferDto dto = new AccountsTransferDto();
    dto.setAmount(depositAmount);
    dto.setCurrency(Currency.EUR.name());
    dto.setPayerAccountNumber((rand < 5) ? accountNumber1 : accountNumber2);
    dto.setPayeeAccountNumber((rand > 4) ? accountNumber1 : accountNumber2);

    return given().contentType(ContentType.JSON).body(dto).log().all().when().post("/account/transfer").statusCode();
  }

  private AccountDto getAccount(String accountNumber)
  {
    return given()
        .contentType(ContentType.JSON)
        .pathParam("accountNumber", accountNumber).log().all()
        .when()
        .get("/account/{accountNumber}").as(AccountDto.class);
  }
}
