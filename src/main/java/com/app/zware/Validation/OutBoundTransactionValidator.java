package com.app.zware.Validation;


import com.app.zware.Entities.OutboundTransaction;
import com.app.zware.HttpEntities.OutboundDetailDTO;
import com.app.zware.HttpEntities.OutboundTransactionDTO;
import com.app.zware.Repositories.OutboundTransactionDetailRepository;
import com.app.zware.Repositories.OutboundTransactionRepository;
import com.app.zware.Repositories.UserRepository;
import com.app.zware.Repositories.WarehouseRespository;
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

  public String checkPut(Integer id, OutboundTransaction outboundTransaction) {
    if (id == null || !outboundTransactionRepository.existsById(id)) {
      return "Not found OutboundTransactionID";
    }
    List<String> statusList = Arrays.asList("Pending", "Processing", "Done", "Cancel");
    if (!statusList.contains(outboundTransaction.getStatus())) {
      return "Status is not valid";
    }
    return checkPost(outboundTransaction);

  }

  public String checkGet(Integer id) {
    if (!checkId(id)) {
      return "Not found ID";
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
