package com.app.zware.HttpEntities;

import java.time.LocalDate;
import java.util.ArrayList;
import lombok.Data;

@Data
public class InboundTransactionDTO {
  private Integer warehouse_id;
  private String source;

  private ArrayList<InboundDetailDTO> details;

  //date, maker_id, status: Auto

}
