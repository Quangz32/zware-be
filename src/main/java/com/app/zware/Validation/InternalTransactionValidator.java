package com.app.zware.Validation;

import com.app.zware.HttpEntities.InternalDetailDTO;
import com.app.zware.HttpEntities.InternalTransactionDTO;
import com.app.zware.Service.InternalTransactionDetailService;
import com.app.zware.Service.InternalTransactionService;
import com.app.zware.Service.ProductService;
import com.app.zware.Service.WarehouseItemsService;
import com.app.zware.Service.WarehouseService;
import com.app.zware.Service.WarehouseZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InternalTransactionValidator {

  @Autowired
  InternalTransactionDetailService internalTransactionDetailService;

  @Autowired
  WarehouseService warehouseService;

  @Autowired
  ProductService productService;

  @Autowired
  WarehouseZoneService zoneService;

  @Autowired
  InternalTransactionService internalTransactionService;

  @Autowired
  WarehouseItemsService warehouseItemsService;

  public String checkCreate(InternalTransactionDTO transactionDTO) {
    //Check REQUIRED
    if (transactionDTO.getType() == null || transactionDTO.getDestination_warehouse() == null ||
        transactionDTO.getSource_warehouse() == null || transactionDTO.getDetails() == null) {
      return "Type, Source Warehouse, Destination Warehouse and Details are required ";
    }

    if (transactionDTO.getDetails().isEmpty()) {
      return "Details cannot be empty";
    }

    //check condition
    if (!transactionDTO.getType().equals("inbound") &&
        !transactionDTO.getType().equals("outbound")) {
      return "Type is NOT valid, valid value: 'inbound' or 'outbound'";
    }
    if (!warehouseService.existById(transactionDTO.getDestination_warehouse())) {
      return "Destination warehouse is NOT valid";
    }
    if (!warehouseService.existById(transactionDTO.getSource_warehouse())) {
      return "Source warehouse is NOT valid";
    }

    if (transactionDTO.getSource_warehouse().equals(
        transactionDTO.getDestination_warehouse()
    )){
      return "Cannot make transaction in same warehouse";
    }

    for (InternalDetailDTO detail : transactionDTO.getDetails()) {
      String checkDetailMessage = this.checkDetail(detail, transactionDTO);
      if (!checkDetailMessage.isEmpty()) {
        return checkDetailMessage;
      }
    }

    return "";
  }

  public String checkDetail(InternalDetailDTO detailDTO, InternalTransactionDTO transactionDTO) {

    //Check REQUIRE
    if (detailDTO.getProduct_id() == null || detailDTO.getQuantity() == null) {
      return "Product and Quantity are required in each detail";
    }

    if (transactionDTO.getType().equals("inbound") &&
        detailDTO.getDestination_zone() == null) {
      return "Destination zone are required in Inbound Internal Transaction";
    }
    if (transactionDTO.getType().equals("outbound") &&
        detailDTO.getDestination_zone() != null) {
      return "Destination zone are not allowed in Outbound Internal Transaction";
    }

    //check valid
    if (!productService.existById(detailDTO.getProduct_id())) {
      return "Product id [" + detailDTO.getProduct_id() + "] is not valid";
    }
    if (detailDTO.getQuantity() < 1) {
      return "Quantity must be more than 0";
    }

    if (detailDTO.getDestination_zone() != null &&
        zoneService.existById(detailDTO.getDestination_zone())) {
      return "Destination_zone is not valid [" + detailDTO.getDestination_zone() + "]";
    }

    //check condition
    if (detailDTO.getDestination_zone() != null &&
        !transactionDTO.getDestination_warehouse().equals(
        warehouseService.getByZone(detailDTO.getDestination_zone()).getId()
    )) {
      return "Zone [" + detailDTO.getDestination_zone()
          + "] is not belong to Desination warehouse ["
          + transactionDTO.getDestination_warehouse() + "]";
    }

    //check số lượng có đủ không
    int quantityInSourceWarehouse = warehouseItemsService.getQuantityNonExpiredByProductAndWarehouse(
        detailDTO.getProduct_id(), transactionDTO.getSource_warehouse());

    if (quantityInSourceWarehouse < detailDTO.getQuantity()) {
      return "Quantity of non-expired product " + detailDTO.getProduct_id()
          + " is not enough in warehouse [" + transactionDTO.getSource_warehouse()
          + "] (Available: " + quantityInSourceWarehouse + " )";
    }

    return "";
  }
}
