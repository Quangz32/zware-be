package com.app.zware.Validation;


import com.app.zware.Entities.OutboundTransaction;
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
  OutboundTransactionService outboundTransactionService;

  @Autowired
  WarehouseService warehouseService;

  @Autowired
  ProductService productService;

  @Autowired
  WarehouseItemsService warehouseItemsService;


  public String checkCreate(OutboundTransactionDTO transactionDTO) {
//REQUIRED
    System.out.println(transactionDTO.toString());
    if (transactionDTO.getWarehouse_id() == null) {
      return "Warehouse Id is required";
    }

    if (transactionDTO.getDestination() == null
        && transactionDTO.getExternal_destination() == null) {
      return "Destination or External destination is required";
    }

    if (transactionDTO.getDestination() != null
        && transactionDTO.getExternal_destination() != null) {
      return "Only Destination or external Destination, not both";
    }

    if (transactionDTO.getDestination() != null &&
        transactionDTO.getDestination().equals(transactionDTO.getWarehouse_id())) {
      return "Cannot outbound to same warehouse";
    }

    if (transactionDTO.getDetails() == null ||
        transactionDTO.getDetails().isEmpty()) {
      return "Details are required";
    }

    //CONDITION CHECK
    if (!warehouseService.existById(transactionDTO.getWarehouse_id())) {
      return "Warehouse ID is not valid";
    }

    if (transactionDTO.getDestination() != null &&
        !warehouseService.existById(transactionDTO.getDestination())) {
      return "Destination is not valid (warehouseId not exist)";
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
    if (detail.getProduct_id() == null || detail.getQuantity() == null
    ) {
      return "Product Id and Quantity are required in each detail";
    }

    //CHECK CONDITION
    if (!productService.existById(detail.getProduct_id())) {
      return "Product Id is not valid: " + detail.getProduct_id();
    }

    int quantityInWarehouse =
        warehouseItemsService.getTotalQuantityByProductIdAndWarehouseId(
            detail.getProduct_id(), transactionDTO.getWarehouse_id()
        );

    if (quantityInWarehouse < detail.getQuantity()) {
      return "Quantity of product " + detail.getProduct_id() + " is not enough (Available: "
          + quantityInWarehouse + " )";
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
//        if( outboundTransaction.getStatus()== null || outboundTransaction.getStatus().isEmpty()){
//            return "Status is invalid";
//        }

//        List<String> statusList = Arrays.asList("pending", "processing", "done", "cancel");
//        if (!statusList.contains(outboundTransaction.getStatus())) {
//            return "Status is not valid";
//        }

    Integer destination = outboundTransaction.getDestination();
    if (destination == null && outboundTransaction.getExternal_destination() == null) {
      return "Destination and External Destination are invalid !";
    } else {
      if (!warehouseRespository.existByIdAndIsDeletedFalse(outboundTransaction.getDestination())) {
        return "Not found ID warehouse for destination";
      }

      if (outboundTransaction.getExternal_destination().isEmpty()) {
        return "External destination is invalid";
      }
    }
    return "";
  }

  public String checkChangeStatus(Integer id, OutboundTransaction transaction) {
    if (id == null || !outboundTransactionService.existById(id)) {
      return "Not found OutboundTransactionID";
    }
    OutboundTransaction oldTransaction = outboundTransactionService.getOutboundTransactionById(id);

    String oldStatus = oldTransaction.getStatus();
    String newStatus = transaction.getStatus();

    // Chỉ cho phép thay đổi trạng thái theo các quy tắc
    if (oldStatus.equals("cancel") || oldStatus.equals("completed")) {
      return "Outbound Transactions has been " + oldStatus+", You are not allowed to change.";
    }
    //not accept change Date of transaction
    if(transaction.getDate() != null && !transaction.getDate().equals(oldTransaction.getDate())) {
      return "You are only allowed to change status.";
    }
    //not accept change destination of transaction
    if(transaction.getDestination() != null && !transaction.getDestination().equals(oldTransaction.getDestination())) {
      return "You are only allowed to change status.";
    }
    //not accept change external_destination of transaction
    if(transaction.getExternal_destination() != null && !transaction.getExternal_destination().equals(oldTransaction.getExternal_destination())) {
      return "You are only allowed to change status.";
    }
    //not accept change maker of transaction
    if(transaction.getMaker_id() != null && !transaction.getMaker_id().equals(oldTransaction.getMaker_id())) {
      return "You are only allowed to change status.";
    }
    //not accept change warehouse of transaction
    if(transaction.getWarehouse_id() != null && !transaction.getWarehouse_id().equals(oldTransaction.getWarehouse_id())) {
      return "You are only allowed to change status.";
    }
    // only change pending -> processing || pending -> cancel
    if (oldStatus.equals("pending") && !(newStatus.equals("processing") || newStatus.equals("cancel"))) {
      return "You can only change status from pending to processing or cancel.";
    }
    // only change processing -> completed || processing -> cancel
    if (oldStatus.equals("processing") && !(newStatus.equals("completed") || newStatus.equals("cancel"))) {
      return "You can only change status from processing to completed or cancel.";
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

  public String checkGetDetail(Integer id) {
    if (!checkId(id)) {
      return "OutboundID not found or OutboundID was deleted !";
    }
    return "";
  }
}
