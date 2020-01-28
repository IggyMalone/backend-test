package com.ijeremic.backendtest.rest.dto;

import com.ijeremic.backendtest.model.AccountTransaction;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Iggy on 28-Jan-2020
 */
@XmlRootElement
@Data
@NoArgsConstructor
public class TransferResponseDtos
{
  private List<TransferResponseDto> transfers;

  public static TransferResponseDtos fromTransactions(List<AccountTransaction> transactions)
  {
    TransferResponseDtos dto = new TransferResponseDtos();
    List<TransferResponseDto> dtoList = new ArrayList<>();

    dtoList.addAll(transactions.stream().map(TransferResponseDto::new).collect(Collectors.toList()));
    dto.setTransfers(dtoList);
    return dto;
  }

}
