package com.app.zware.Validation;

import com.app.zware.HttpEntities.InWarehouseTransactionDTO;
import com.app.zware.Service.WarehouseZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component

public class InWarehouseTransactionValidator {

    @Autowired
    WarehouseZoneService warehouseZoneService;


    public String check(InWarehouseTransactionDTO inWarehouseTransactionDTO){
        if (inWarehouseTransactionDTO.getWarehouse_id()==null||inWarehouseTransactionDTO.getSource_zone()==null||inWarehouseTransactionDTO.getDestination_zone()==null||
        inWarehouseTransactionDTO.getDetails()==null){
            return "Warehouse Id , Source Zone , Source Destination , Details are required";
        }
        if(inWarehouseTransactionDTO.getDetails().isEmpty()){
            return "Details can not be empty";
        }
        if (warehouseZoneService.findByIdAndWarehouseId(inWarehouseTransactionDTO.getSource_zone(), inWarehouseTransactionDTO.getWarehouse_id())==null){
            return "Source Zone is not valid";
        }
        if (warehouseZoneService.findByIdAndWarehouseId(inWarehouseTransactionDTO.getDestination_zone(), inWarehouseTransactionDTO.getWarehouse_id())==null){
            return "Source Destination is not valid";
        }

   return "";
    }
}
