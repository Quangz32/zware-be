package com.app.zware.Validation;

import com.app.zware.Entities.GoodsDisposal;
import com.app.zware.Entities.WarehouseItems;
import com.app.zware.HttpEntities.DisposedGoodsDTO;
import com.app.zware.HttpEntities.GoodsDisposalDTO;
import com.app.zware.Repositories.GoodsDisposalRepository;
import com.app.zware.Repositories.UserRepository;
import com.app.zware.Repositories.WarehouseRespository;
import com.app.zware.Service.ProductService;
import com.app.zware.Service.WarehouseItemsService;
import com.app.zware.Service.WarehouseService;
import com.app.zware.Service.WarehouseZoneService;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsDisposalValidator {

  @Autowired
  GoodsDisposalRepository goodsDisposalRepository;

  @Autowired
  WarehouseRespository warehouseRespository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  WarehouseService warehouseService;

  @Autowired
  ProductService productService;

  @Autowired
  WarehouseItemsService warehouseItemsService;

  @Autowired
  WarehouseZoneService zoneService;

  public String checkCreate(GoodsDisposalDTO disposalDTO) {
    //check required
    if (disposalDTO.getWarehouse_id() == null) {
      return "Warehouse Id is required";
    }

    if (disposalDTO.getDetails() == null || disposalDTO.getDetails().isEmpty()) {
      return "Details is required";
    }

    //check condition
    if (!warehouseService.existById(disposalDTO.getWarehouse_id())) {
      return "Warehouse Id is not valid";
    }

    Set<DisposedGoodsDTO> detailSet = new HashSet<>();
    for (DisposedGoodsDTO detail : disposalDTO.getDetails()) {
      if (!detailSet.add(detail)){
        return "Details cannot be duplicate in Zone id, Product id and Expire date";
      }
      String checkMessage = this.checkCreateDetail(detail, disposalDTO);
      if (!checkMessage.isEmpty()) {
        return checkMessage;
      }
    }

    return "";
  }

  public String checkCreateDetail(DisposedGoodsDTO detail, GoodsDisposalDTO disposalDTO) {
    //check require
    if (detail.getZone_id() == null || detail.getProduct_id() == null ||
        detail.getExpire_date() == null || detail.getQuantity() == null ||
        detail.getReason() == null || detail.getReason().isEmpty()) {

      return "Zone Id, Product Id, Expire date, Quantity and Reason is required in each detail";
    }

    //check condition
    if (!zoneService.existById(detail.getZone_id())) {
      return "Zone id is not valid";
    }

    if (!warehouseService.getByZone(detail.getZone_id()).getId()
        .equals(disposalDTO.getWarehouse_id())) {
      return "Zone id [" + detail.getZone_id() + "] is not belong to warehouse ["
          + disposalDTO.getWarehouse_id() + "]";
    }

    if (!productService.existById(detail.getProduct_id())) {
      return "Product Id is not valid";
    }

    WarehouseItems wi = warehouseItemsService.findByZoneAndProductAndDate(
        detail.getZone_id(), detail.getProduct_id(), detail.getExpire_date()
    );

    if (wi == null) { //not found warehouseItem
      return "There are no product [" + detail.getProduct_id() + "] in zone [" + detail.getZone_id()
          + "] with expire date " + detail.getExpire_date();
    }

    if (wi.getQuantity() < detail.getQuantity()) {  //not enough quantity
      return "Quantity of product [" + detail.getProduct_id() + "] with the expire date "
          + detail.getExpire_date() + " in zone [" + detail.getZone_id()
          + "] is not enough!";
    }

    return "";
  }

  public String checkPost(GoodsDisposal goodsDisposal) {
    if (goodsDisposal.getStatus().isEmpty()) {
      return "Status is not empty";
    }
    if (goodsDisposal.getWarehouse_id() == null) {
      return "Warehouse ID is not empty";
    }
    if (!checkWarehouseExist(goodsDisposal.getWarehouse_id())) {
      return "Warehouse ID does not exist";
    }
    if (goodsDisposal.getDate() == null) {
      return "Date is not empty";
    }

    Integer makerID = goodsDisposal.getMaker_id();
    if (makerID == null || !userRepository.existsById(makerID)) {
      return "Not Found makerID";
    }

    return "";
  }

  public String checkPut(Integer goodsDisposalId, GoodsDisposal goodsDisposal) {
    if (goodsDisposalId == null || !goodsDisposalRepository.existByIdAndIsDeletedFalse(
        goodsDisposalId)) {
      return "Id is not valid";
    }
    return checkPost(goodsDisposal);
  }

  private boolean checkGoodsDisposalId(Integer id) {
    return goodsDisposalRepository.existByIdAndIsDeletedFalse(id);
  }

  public String checkGet(Integer id) {
    if (!checkGoodsDisposalId(id)) {
      return "Id is not valid";
    }
    return "";
  }

  public String checkDelete(Integer id) {
    return checkGet(id);
  }


  private boolean checkIdExist(Integer id) {
    return goodsDisposalRepository.existsById(id);
  }


  private boolean checkWarehouseExist(Integer id) {
    return warehouseRespository.existsById(id);
  }
}
