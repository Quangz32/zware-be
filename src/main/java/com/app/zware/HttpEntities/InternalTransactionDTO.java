package com.app.zware.HttpEntities;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class InternalTransactionDTO {
  String type; //"inbound" or "outbound"
  Integer source_warehouse;
  Integer destination_warehouse;

  List<InternalDetailDTO> details;

  //Date, Maker_id, Status : AUTO
}
