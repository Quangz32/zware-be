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
