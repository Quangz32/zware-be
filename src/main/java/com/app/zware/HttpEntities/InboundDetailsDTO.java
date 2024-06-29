package com.app.zware.HttpEntities;

import lombok.Data;

import java.time.LocalDate;
@Data
public class InboundDetailsDTO {
    Integer product_id;

    LocalDate expire_date;

    Integer quantity;

    Integer zone_id;

}
