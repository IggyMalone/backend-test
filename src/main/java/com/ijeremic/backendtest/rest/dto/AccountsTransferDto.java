package com.ijeremic.backendtest.rest.dto;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Iggy on 19-Jan-2020.
 */
@Data
@NoArgsConstructor
public class AccountsTransferDto
{
  @NotBlank(message = "Payer account number most not be blank")
  String payerAccountNumber;

  @NotBlank(message = "Payee account number most not be blank")
  String payeeAccountNumber;

  @NotBlank(message = "Transfer amount most not be blank")
  BigDecimal amount;

  @NotBlank(message = "Transfer amount currency most not be blank")
  String currency;

  Boolean approveImmediately;
}
