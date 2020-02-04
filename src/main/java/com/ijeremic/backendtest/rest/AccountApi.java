package com.ijeremic.backendtest.rest;

import com.ijeremic.backendtest.logic.AccountService;
import com.ijeremic.backendtest.model.enumeration.Currency;
import com.ijeremic.backendtest.rest.dto.AccountDto;
import com.ijeremic.backendtest.rest.dto.AccountsTransferDto;
import com.ijeremic.backendtest.rest.dto.PaymentDto;
import com.ijeremic.backendtest.rest.dto.TransferResponseDto;
import com.ijeremic.backendtest.rest.dto.TransferResponseDtos;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * REST service for resources related to Account
 *
 * Created by Iggy on 19-Jan-2020.
 */
@Path("/account")
public class AccountApi
{
  @Inject
  private AccountService accountService;

  /**
   * Method used to expose account data
   *
   * @param accountNumber account number to search data by
   *
   * @return account information
   *
   * @throws Exception
   */
  @GET
  @Path("/{accountNumber}")
  @Produces(MediaType.APPLICATION_JSON)
  public AccountDto accountData(@PathParam("accountNumber") String accountNumber)
      throws Exception
  {
    return AccountDto.fromAccount(accountService.getAccountData(accountNumber));
  }

  /**
   * Method used to deposit money to an account
   *
   * @param accountNumber account number to deposit money to
   * @param paymentDto amount and currency
   *
   * @return transaction information
   *
   * @throws Exception
   */
  @PATCH
  @Path("/{accountNumber}/deposit")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public TransferResponseDto depositToAccount(@PathParam("accountNumber") String accountNumber, PaymentDto paymentDto)
      throws Exception
  {
    return new TransferResponseDto(accountService.depositToAccount(accountNumber, paymentDto.getAmount(), Currency.valueOf(paymentDto.getCurrency())));
  }

  /**
   * Method used to charge account for an amount of money
   *
   * @param accountNumber account number to charge money from
   * @param paymentDto amount and currency
   *
   * @return transaction information
   *
   * @throws Exception
   */
  @PATCH
  @Path("/{accountNumber}/charge")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public TransferResponseDto chargeAccount(@PathParam("accountNumber") String accountNumber, PaymentDto paymentDto)
      throws Exception
  {
    return new TransferResponseDto(accountService.chargeAccount(accountNumber, paymentDto.getAmount(), Currency.valueOf(paymentDto.getCurrency())));
  }

  /**
   * Method used to transfer money between accounts
   *
   * @param accountsTransferDto account numbers, money, currency
   *
   * @return transaction information
   *
   * @throws Exception
   */
  @POST
  @Path("/transfer")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public TransferResponseDtos transferBetweenAccounts(AccountsTransferDto accountsTransferDto) throws Exception
  {
    return TransferResponseDtos.fromTransactions(accountService.transferBetweenAccounts(accountsTransferDto.getPayerAccountNumber(),
        accountsTransferDto.getPayeeAccountNumber(), accountsTransferDto.getAmount(), Currency.valueOf(accountsTransferDto.getCurrency()))
    );
  }
}
