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

    if (transactionDTO.getSource() == null || transactionDTO.getSource().isEmpty()) {
      return "Source or External source is required";
    }

    if (transactionDTO.getDetails() == null ||
        transactionDTO.getDetails().isEmpty()) {
      return "Details are required";
    }

    //CONDITION CHECK
    if (!warehouseService.existById(transactionDTO.getWarehouse_id())) {
      return "Warehouse ID is not valid";
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
    if (detail.getProduct_id() == null || detail.getExpire_date() == null
        || detail.getQuantity() == null || detail.getZone_id() == null
    ) {
      return "Product Id, Expire date, Quantity and ZoneId are required in each detail";
    }

    //CHECK CONDITION
    if (!productService.existById(detail.getProduct_id())) {
      return "Product Id is not valid: " + detail.getProduct_id();
    }

    if (LocalDate.now().isAfter(detail.getExpire_date())) {
      return "Expire date cannot be in the past";
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

    List<String> statusList = Arrays.asList("pending", "processing", "completed", "canceled");
    if (!statusList.contains(inboundTransaction.getStatus())) {
      return "Status is not valid";
    }

    //check Source HERE

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
