package com.app.zware.Validation;

import com.app.zware.Entities.OutboundTransactionDetail;
import com.app.zware.Entities.WarehouseItems;
import com.app.zware.HttpEntities.OutboundTransactionDTO;
import com.app.zware.Repositories.ItemRepository;
import com.app.zware.Repositories.WarehouseItemsRepository;
import com.app.zware.Repositories.WarehouseRespository;
import com.app.zware.Repositories.WarehouseZoneRespository;
import com.app.zware.Service.WarehouseItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OutboundTransactionDtoValidator {
    @Autowired
    WarehouseRespository warehouseRespository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    WarehouseZoneRespository zoneRespository;

    @Autowired
    WarehouseItemsRepository warehouseItemsRepository;

    @Autowired
    WarehouseItemsService warehouseItemsService;

    public String checkPost(OutboundTransactionDTO transactionDTO){
        if(transactionDTO.getDate() == null){
            return "Date is invalid";
        }

        Integer destination = transactionDTO.getDestination();
        String externalDestination = transactionDTO.getExternal_destination();

        if(destination != null && externalDestination != null){
            return "Provide either destination or external destination, not both";
        }

        if(destination == null && externalDestination == null){
            return "Either destination or external destination must be provided";
        }

        if(destination != null && !warehouseRespository.existByIdAndIsDeletedFalse(destination)){
            return "Destination is  invalid";
        }

        if(!transactionDTO.getDetails().isEmpty()){
            for (OutboundTransactionDetail detail : transactionDTO.getDetails()){
                Integer itemId = detail.getItem_id();
                if(itemId == null || !itemRepository.existsByIdAndIsDeletedFalse(itemId)) {
                    return "Item Id is invalid";
                }
                Integer zoneId = detail.getZone_id();
                if (zoneId == null || !zoneRespository.existsById(zoneId)) {
                    return "Zone id is invalid";
                }

                if(detail.getQuantity() == null || detail.getQuantity() < 0){
                    return "Quantity is invalid";
                }
                //check quantity remaining
                WarehouseItems warehouseItem = warehouseItemsRepository.findByZoneIdAndItemId(detail.getZone_id(), detail.getItem_id());
                int availableQuantity = warehouseItem.getQuantity();
                if (detail.getQuantity() > availableQuantity) {
                    return "The quantity of items : " + detail.getItem_id() + " in the Zone : " + detail.getZone_id() + " are not enough." ;
                }
            }
        }
        //delete quantity in zone if transaction success
//        warehouseItemsService.deleteQuantity(transactionDTO);
        return "";
    }
}
