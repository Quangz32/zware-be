package com.app.zware.HttpEntities;

import lombok.Data;

@Data
public class WarehouseInventoryDTO {

    private String productName;
    private String image;
    private String supplier;
    private String measureUnit;
    private Integer warehouseId;
    private Long totalQuantity;


}
