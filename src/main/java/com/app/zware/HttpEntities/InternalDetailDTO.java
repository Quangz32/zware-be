package com.app.zware.HttpEntities;

import lombok.Data;

@Data
public class InternalDetailDTO {
  Integer destination_zone;
  Integer product_id;
  Integer quantity;

  //Auto: item, transaction_id, source_zone;
}
