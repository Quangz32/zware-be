package com.app.zware.Validation;


import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.Entities.WarehouseItems;
import com.app.zware.HttpEntities.OutboundDetailDTO;
import com.app.zware.HttpEntities.OutboundTransactionDTO;
import com.app.zware.Repositories.OutboundTransactionDetailRepository;
import com.app.zware.Repositories.OutboundTransactionRepository;
import com.app.zware.Repositories.UserRepository;
import com.app.zware.Repositories.WarehouseRespository;
import com.app.zware.Service.OutboundTransactionService;
import com.app.zware.Service.ProductService;
import com.app.zware.Service.WarehouseItemsService;
import com.app.zware.Service.WarehouseService;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OutBoundTransactionValidator {

  @Autowired
  OutboundTransactionRepository outboundTransactionRepository;

  @Autowired
  WarehouseRespository warehouseRespository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  OutboundTransactionDetailRepository outboundTransactionDetailRepository;

  @Autowired
  WarehouseService warehouseService;

  @Autowired
  ProductService productService;

  @Autowired
  WarehouseItemsService warehouseItemsService;

  @Autowired
  OutboundTransactionService outboundTransactionService;


  public String checkCreate(OutboundTransactionDTO transactionDTO) {
    //REQUIRED
    if (transactionDTO.getWarehouse_id() == null) {
      return "Warehouse Id is required";
    }

    if (transactionDTO.getDestination() == null && transactionDTO.getDestination().isEmpty()) {
      return "Destination is required";
    }

    if (transactionDTO.getDetails() == null || transactionDTO.getDetails().isEmpty()) {
      return "Details are required";
    }

    //CONDITION CHECK
    if (!warehouseService.existById(transactionDTO.getWarehouse_id())) {
      return "Warehouse ID is not valid";
    }

    for (OutboundDetailDTO detail : transactionDTO.getDetails()) {
      String checkDetailMessage = checkCreateDetail(detail, transactionDTO);
      if (!checkDetailMessage.isEmpty()) {
        return checkDetailMessage;
      }
    }

    return "";
  }

  public String checkCreateDetail(OutboundDetailDTO detail, OutboundTransactionDTO transactionDTO) {
    //CHECK REQUIRE
    if (detail.getProduct_id() == null || detail.getQuantity() == null) {
      return "Product Id and Quantity are required in each detail";
    }

    //CHECK CONDITION
    if (!productService.existById(detail.getProduct_id())) {
      return "Product Id is not valid: " + detail.getProduct_id();
    }

    int quantityInWarehouse = warehouseItemsService.getQuantityNonExpiredByProductAndWarehouse(
        detail.getProduct_id(), transactionDTO.getWarehouse_id());

    if (quantityInWarehouse < detail.getQuantity()) {
      return "Quantity of non-expired product " + detail.getProduct_id()
          + " is not enough (Available: " + quantityInWarehouse + " )";
    }

    return "";

  }

  public String checkPost(OutboundTransaction outboundTransaction) {
    if (outboundTransaction.getDate() == null) {
      return "Date is invalid";
    }

    Integer makerId = outboundTransaction.getMaker_id();
    if (makerId == null || !userRepository.existByIdAndIsDeletedFalse(
        outboundTransaction.getMaker_id())) {
      return "Not found Maker";
    }

    //Check destination HERE

    return "";
  }

  public String checkStatus(Integer id, String status) {
    OutboundTransaction oldTransaction = outboundTransactionService.getOutboundTransactionById(id);
    String oldStatus = oldTransaction.getStatus();
    System.out.println(oldStatus);

    String newStatus = status;
    System.out.println(oldStatus.equals(newStatus));

    // status = completed or cancel, not allowed to change
    if (oldStatus.equals("canceled") || oldStatus.equals("completed")) {
      return "Outbound Transactions has been " + oldStatus + ", You are not allowed to change.";
    }

    if (oldStatus.equals("pending") && !(newStatus.equals("shipping") || newStatus.equals("canceled"))) {
      return "You can only change status from pending to shipping or canceled.";
    }
    if (oldStatus.equals("processing") && !(newStatus.equals("completed") || newStatus.equals("canceled"))) {
      return "You can only change status from processing to completed or canceled.";
    }
    return "";
  }

  public String checkGet(Integer id) {
    if (!checkId(id)) {
      return "Outbound transaction Id is not valid";
    } else {
      return "";
    }
  }

  public String checkDelete(Integer id) {
    return checkGet(id);
  }

  public boolean checkId(Integer id) {
    return outboundTransactionRepository.existsByIdAndIsDeletedFalse(id);
  }

  public String checkQuantity(Integer zoneId, Integer itemId, Integer quantity) {
    Integer quantityInWarehouse = warehouseItemsService.getQuantityNonExpiredByItemAndZone(itemId, zoneId);

    if(quantityInWarehouse < quantity){
      return "Quantity of non-expired product ( item: " + itemId + " and zone: " + zoneId
              + ") is not enough to shipping (Available: " + quantityInWarehouse + " )";
    }
    return "";
  }
}
