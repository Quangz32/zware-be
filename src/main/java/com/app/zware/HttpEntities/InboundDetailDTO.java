package com.app.zware.HttpEntities;

import java.time.LocalDate;
import lombok.Data;

@Data
public class InboundDetailDTO {
  private Integer product_id;
  private LocalDate expire_date;
  private Integer quantity;
  private Integer zone_id;
}
