package com.ijeremic.backendtest.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ijeremic.backendtest.model.enumeration.Currency;
import com.ijeremic.backendtest.rest.dto.AccountDto;
import com.ijeremic.backendtest.rest.dto.AccountsTransferDto;
import com.ijeremic.backendtest.rest.dto.PaymentDto;
import com.ijeremic.backendtest.rest.dto.TransferResponseDto;
import com.ijeremic.backendtest.rest.dto.TransferResponseDtos;
import com.ijeremic.backendtest.util.JerseyInjectionBinder;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.util.List;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Created by Iggy on 27-Jan-2020
 */
public class AccountApiTest
{
  static Server server;

  @BeforeAll
  public static void setup() throws Exception
  {
    RestAssured.port = Integer.valueOf(8000);
    RestAssured.basePath =  "/";
    RestAssured.baseURI = "http://localhost";

    server = new Server(8000);
    ResourceConfig config = new ResourceConfig();
    config.packages("com.ijeremic.backendtest.rest");
    config.register(new JerseyInjectionBinder());
    ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
    ServletContextHandler contextHandler = new ServletContextHandler(server, "/");
    contextHandler.addServlet(jerseyServlet, "/*");

    server.start();
  }

  @AfterAll
  public static void teardown() throws Exception
  {
    server.stop();
  }

  @DisplayName("200 test accountData()")
  @Test
  void accountDataTest()
  {
    given()
        .pathParam("accountNumber", "1000001000").log().all()
    .when()
        .get("/account/{accountNumber}").then().log().body().assertThat().statusCode(200);
  }

  @DisplayName("404 test accountData()")
  @Test
  void accountDataMissingTest()
  {
    given()
        .pathParam("accountNumber", "rrrrrr").log().all()
        .when()
        .get("/account/{accountNumber}").then().log().body().assertThat().statusCode(404);
  }

  @DisplayName("200 test with balance check on deposit()")
  @Test
  void depositTest()
  {
    BigDecimal depositAmount = BigDecimal.ONE;
    AccountDto accountDto = getAccount("1000001000");
    PaymentDto dto = stubPaymentDto(depositAmount);

    TransferResponseDto transferResponseDto = given()
        .contentType(ContentType.JSON)
        .pathParam("accountNumber", "1000001000")
        .body(dto)
        .log().all()
        .when()
        .patch("/account/{accountNumber}/deposit").then().log().body().assertThat().statusCode(200)
        .and()
        .extract().response().as(TransferResponseDto.class);

    assertEquals(accountDto.getBalance().add(depositAmount), transferResponseDto.getAccount().getBalance());
  }

  @DisplayName("404 test on deposit()")
  @Test
  void depositMissingAccountTest()
  {
    PaymentDto dto = stubPaymentDto(BigDecimal.ONE);

    given()
        .contentType(ContentType.JSON)
        .pathParam("accountNumber", "rrrrrrrr")
        .body(dto)
        .log().all()
        .when()
        .patch("/account/{accountNumber}/deposit").then().log().body().assertThat().statusCode(404);
  }

  @DisplayName("200 test with balance check on charge()")
  @Test
  void chargeTest()
  {
    BigDecimal depositAmount = BigDecimal.ONE;
    AccountDto accountDto = getAccount("1000001000");
    PaymentDto dto = stubPaymentDto(depositAmount);

    TransferResponseDto transferResponseDto = given()
        .contentType(ContentType.JSON)
        .pathParam("accountNumber", "1000001000")
        .body(dto)
        .log().all()
        .when()
        .patch("/account/{accountNumber}/charge").as(TransferResponseDto.class);

    assertEquals(accountDto.getBalance().subtract(depositAmount), transferResponseDto.getAccount().getBalance());
  }

  @DisplayName("404 test on charge()")
  @Test
  void chargeMissingAccountTest()
  {
    PaymentDto dto = stubPaymentDto(BigDecimal.ONE);

    given()
        .contentType(ContentType.JSON)
        .pathParam("accountNumber", "rrrrrrrr")
        .body(dto)
        .log().all()
        .when()
        .patch("/account/{accountNumber}/charge").then().log().body().assertThat().statusCode(404);
  }

  @DisplayName("200 test with balance check on transferBetweenAccounts()")
  @Test
  void transferBetweenAccountsTest()
  {
    BigDecimal depositAmount = BigDecimal.ONE;
    AccountDto payerAccountDto = getAccount("1000001000");
    AccountDto payeeAccountDto = getAccount("1000001001");
    AccountsTransferDto dto = new AccountsTransferDto();
    dto.setAmount(depositAmount);
    dto.setCurrency(Currency.EUR.name());
    dto.setPayerAccountNumber("1000001000");
    dto.setPayeeAccountNumber("1000001001");

    TransferResponseDtos transferResponseDtos = given()
        .contentType(ContentType.JSON)
        .body(dto)
        .log().all()
        .when()
        .post("/account/transfer").as(TransferResponseDtos.class);

    List<TransferResponseDto> transfers = transferResponseDtos.getTransfers();

    assertEquals(transfers.size(), 2);
    TransferResponseDto payerDto = transfers.get(0);
    TransferResponseDto payeeDto = transfers.get(1);


    assertEquals(payerAccountDto.getBalance().subtract(depositAmount), payerDto.getAccount().getBalance());
    assertEquals(payeeAccountDto.getBalance().add(depositAmount), payeeDto.getAccount().getBalance());
  }

  private AccountDto getAccount(String accountNumber)
  {
    return given()
        .contentType(ContentType.JSON)
        .pathParam("accountNumber", accountNumber).log().all()
        .when()
        .get("/account/{accountNumber}").as(AccountDto.class);
  }

  private PaymentDto stubPaymentDto(BigDecimal amount)
  {
    PaymentDto dto = new PaymentDto();
    dto.setAmount(amount);
    dto.setCurrency(Currency.EUR.name());

    return dto;
  }
}
