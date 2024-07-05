package com.app.zware.HttpEntities;

import java.time.LocalDate;
import java.util.ArrayList;
import lombok.Data;

@Data
public class OutboundTransactionDTO {
  private Integer warehouse_id;
  private String destination;
  private ArrayList<OutboundDetailDTO> details;

  //date, maker_id, status : Auto
}
