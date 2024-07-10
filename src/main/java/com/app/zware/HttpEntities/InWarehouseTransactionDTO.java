package com.app.zware.HttpEntities;

import lombok.Data;

import java.util.ArrayList;

@Data
public class InWarehouseTransactionDTO {
    private Integer warehouse_id;
    private Integer source_zone;
    private Integer destination_zone;
    private ArrayList<InWarehouseDetailDTO> details;



}
