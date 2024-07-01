package com.app.zware.HttpEntities;

import java.time.LocalDate;
import java.util.ArrayList;
import lombok.Data;

@Data
public class InboundTransactionDTO {
  private Integer warehouse_id;
//  private LocalDate date;   AUTO
//  private Integer maker_id; AUTO
  //  private String status;  AUTO
  private Integer source;
  private String external_source;

  private ArrayList<InboundDetailDTO> details;

}
