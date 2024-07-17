package com.app.zware.HttpEntities;

import lombok.Data;

@Data
public class WarehouseInventoryDTO {

    private String productName;
    private String supplier;
    private String measureUnit;
    private Long totalQuantity;


}
