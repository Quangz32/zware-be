package com.app.zware.HttpEntities;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutboundDetailDTO {
  private Integer product_id;
//  private LocalDate expire_date;
  private Integer quantity;
//  private Integer zone_id;  AUTO

}
