package com.app.zware.HttpEntities;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutboundDetailDTO {
  private Integer product_id;
  private Integer quantity;

  //transaction_id, item_id, zone_id: Auto
}
