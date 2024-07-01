package com.app.zware.Validation;

import com.app.zware.Entities.InboundTransaction;
import com.app.zware.Entities.WarehouseZone;
import com.app.zware.HttpEntities.InboundDetailDTO;
import com.app.zware.HttpEntities.InboundTransactionDTO;
import com.app.zware.Repositories.InboundTransactionRepository;
import com.app.zware.Repositories.OutboundTransactionRepository;
import com.app.zware.Repositories.UserRepository;
import com.app.zware.Service.OutboundTransactionService;
import com.app.zware.Service.ProductService;
import com.app.zware.Service.UserService;
import com.app.zware.Service.WarehouseItemsService;
import com.app.zware.Service.WarehouseService;
import com.app.zware.Service.WarehouseZoneService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InboundTransactionValidator {

  @Autowired
  InboundTransactionRepository inboundTransactionRepository;

  @Autowired
  OutboundTransactionRepository outboundTransactionRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  UserService userService;

  @Autowired
  WarehouseService warehouseService;

  @Autowired
  OutboundTransactionService outboundTransactionService;

  @Autowired
  ProductService productService;

  @Autowired
  WarehouseZoneService warehouseZoneService;

  @Autowired
  WarehouseItemsService warehouseItemsService;

  private boolean checkInboundTransactionId(Integer id) {
    return inboundTransactionRepository.existByIdAndIsDeletedFalse(id);
  }

  public String checkCreate(InboundTransactionDTO transactionDTO) {
    //REQUIRED
    if (transactionDTO.getWarehouse_id() == null) {
      return "Warehouse Id is required";
    }

    if (transactionDTO.getSource() == null && transactionDTO.getExternal_source() == null) {
      return "Source or External source is required";
    }

    if (transactionDTO.getSource() != null && transactionDTO.getExternal_source() != null) {
      return "Only source or external source, not both";
    }

    if (transactionDTO.getSource() != null &&
        transactionDTO.getSource().equals(transactionDTO.getWarehouse_id())) {
      return "Cannot inbound from same warehouse";
    }

    if (transactionDTO.getDetails() == null ||
        transactionDTO.getDetails().isEmpty()) {
      return "Details are required";
    }

    //CONDITION CHECK
    if (!warehouseService.existById(transactionDTO.getWarehouse_id())) {
      return "Warehouse ID is not valid";
    }

    if (transactionDTO.getSource() != null &&
        !warehouseService.existById(transactionDTO.getSource())) {
      return "Source is not valid";
    }

    for (InboundDetailDTO detail : transactionDTO.getDetails()) {
      String checkDetailMessage = checkCreateDetail(detail, transactionDTO);
      if (!checkDetailMessage.isEmpty()) {
        return checkDetailMessage;
      }
    }

    return "";
  }

  public String checkCreateDetail(InboundDetailDTO detail, InboundTransactionDTO transactionDTO) {
    //CHECK REQUIRE
    if (detail.getProduct_id() == null
        || detail.getQuantity() == null || detail.getZone_id() == null
    ) {
      return "Product Id, Quantity and ZoneId are required in each detail";
    }

    if (transactionDTO.getSource() == null && detail.getExpire_date() == null
    ) {
      return "Expire date is required when the source is external";
    }

    if (transactionDTO.getSource() != null && detail.getExpire_date() != null) {
      return "Expire date is not allowed when source is internal";
    }

    //CHECK CONDITION
    if (!productService.existById(detail.getProduct_id())) {
      return "Product Id is not valid: " + detail.getProduct_id();
    }

    if (transactionDTO.getSource() == null
        && LocalDate.now().isAfter(detail.getExpire_date())) {
      return "Expire date cannot be in the past";
    }

    if (transactionDTO.getSource() != null) {
      int quantityInWarehouse =
          warehouseItemsService.getTotalQuantityByProductIdAndWarehouseId(
              detail.getProduct_id(), transactionDTO.getSource()
          );

      if (quantityInWarehouse < detail.getQuantity()) {
        return "Quantity of product " + detail.getProduct_id() + " is not enough (Available: "+ quantityInWarehouse+" )";
      }
    }


    if (!warehouseZoneService.existById(detail.getZone_id())) {
      return "Zone id is not exist: " + detail.getZone_id();
    } else {
      WarehouseZone zone = warehouseZoneService.getWarehouseZoneById(detail.getZone_id());
      if (!zone.getWarehouse_id().equals(transactionDTO.getWarehouse_id())) {
        return "Zone id " + zone.getId() + " is not valid, it's NOT in the warehouse with id: "
            + transactionDTO.getWarehouse_id();
      }
    }

    return "";
  }

  public String checkGet(Integer id) {
    if (!checkInboundTransactionId(id)) {
      return "TransactionID is not valid";
    }
    return "";
  }

  public String checkPost(InboundTransaction inboundTransaction) {

    if (inboundTransaction.getDate() == null) {
      return "Transaction date is not valid";
    }

    Integer makerId = inboundTransaction.getMaker_id();
    if (makerId == null || !userRepository.existsById(makerId)) {
      return "Maker id is not valid";
    }

    List<String> statusList = Arrays.asList("Pending", "Processing", "Done", "Cancel");
    if (!statusList.contains(inboundTransaction.getStatus())) {
      return "Status is not valid";
    }

    Integer source = inboundTransaction.getSource();
    if (source != null && !outboundTransactionRepository.existsById(source)) {
      return "Source is not valid";
    }
    if (inboundTransaction.getExternal_source() == null) {
      return "External source is invalid";
    }

    return "";
  }

  public String checkPut(Integer transactionId, InboundTransaction transaction) {
    if (transactionId == null || !inboundTransactionRepository.existByIdAndIsDeletedFalse(
        transactionId)) {
      return "Id is not valid";
    }
    return checkPost(transaction);
  }

  public String checkDelete(Integer id) {
    return checkGet(id);
  }

}
