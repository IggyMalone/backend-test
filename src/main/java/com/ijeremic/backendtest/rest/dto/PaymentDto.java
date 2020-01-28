package com.ijeremic.backendtest.rest.dto;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Iggy on 19-Jan-2020.
 */
@XmlRootElement
@Data
@NoArgsConstructor
public class PaymentDto
{
  BigDecimal amount;
  String currency;
}
