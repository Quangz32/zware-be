package com.app.zware.Validation;

import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.OutboundTransactionDTO;
import com.app.zware.Repositories.WarehouseItemsRepository;
import com.app.zware.Repositories.WarehouseRespository;
import com.app.zware.Service.OutboundTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboundTransactionDtoValidator {
    @Autowired
    WarehouseRespository warehouseRespository;

    @Autowired
    OutboundTransactionService outboundTransactionService;

    @Autowired
    WarehouseItemsRepository warehouseItemsRepository;


    public String checkPost(OutboundTransactionDTO transactionDTO, User user){
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
        //if admin create
        if(user.getWarehouse_id() == null) {
            return "You are Admin, so you need enter warehouse to outbound transaction.";
        }
        if(!warehouseRespository.existByIdAndIsDeletedFalse(transactionDTO.getWarehouse_id())){
            return "Warehouse is  not exist";
        }
        Integer warehouseId;
        if(user.getWarehouse_id() != null) {
            warehouseId = user.getWarehouse_id();
        } else {
            warehouseId = transactionDTO.getWarehouse_id();
        }
        String quantityValidationMessage = checkQuantityForTransaction(transactionDTO, warehouseId);
        if (!quantityValidationMessage.isEmpty()) {
            return quantityValidationMessage;
        }
        return "";
    }

    // Method to check quantity for the outbound transaction
    private String checkQuantityForTransaction(OutboundTransactionDTO transactionDTO, Integer warehouseId) {
        List<OutboundTransactionDTO.TransactionItem> listDetail = transactionDTO.getTransactionItems();
        for (OutboundTransactionDTO.TransactionItem detail : listDetail) {
        Integer quantityTransaction = detail.getQuantityProduct();
        Integer availableQuantity = warehouseItemsRepository.sumQuantityByProductIdAndWarehouseId(detail.getProductId(), warehouseId);
        if(availableQuantity == null) {
            return "Not found product in warehouse";
        }
        if (quantityTransaction > availableQuantity) {
                return "The quantity of Product with id : "+ detail.getProductId() +" are not enough.";
            }
        }
        return "";
    }

    public String checkPut(Integer id, OutboundTransaction outboundTransaction){
        if(outboundTransaction.getStatus().equals("done")){
            //set quantity in warehouseItem
            outboundTransactionService.updateWarehouseItemsQuantities(id, outboundTransaction);
            return "Update quantity of Products in warehouse";
        }
        if(outboundTransaction.getStatus().equals("cancel")) {
            //delete outbound transaction
            outboundTransactionService.deleteOutboundTransaction(id);
            return "Cancel outbound transaction";
        }
        return "Not change";
    }
}

