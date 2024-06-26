package com.app.zware.Service;

import com.app.zware.Entities.InboundTransactionDetail;
import com.app.zware.Entities.OutboundTransactionDetail;
import com.app.zware.Entities.WarehouseItems;
import com.app.zware.Repositories.WarehouseItemsRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WarehouseItemsService {

  @Autowired
  WarehouseItemsRepository warehouseItemsRepository;

  @Autowired
  ItemService itemService;

  public List<WarehouseItems> getAllWarehouseItems() {
    return warehouseItemsRepository.findAll();
  }


  public WarehouseItems getById(Integer id) {
    return warehouseItemsRepository.findById(id)
        .orElse(null); // Return null if item is not found
  }

  public WarehouseItems createWarehouseItems(WarehouseItems request) {
    request.setIsdeleted(false);
    return warehouseItemsRepository.save(request);
  }

  public void deleteWarehouseItemsById(Integer id) {
    WarehouseItems warehouseItems = getById(id);
    warehouseItems.setIsdeleted(true);
    warehouseItemsRepository.save(warehouseItems);
  }

  public void deleteWarehouseItemsByZoneId(Integer id) {
    List<WarehouseItems> warehouseItems = warehouseItemsRepository.findZoneId(id);
    for (WarehouseItems warehouseItemsToDelete : warehouseItems) {
      warehouseItemsToDelete.setIsdeleted(true);
      warehouseItemsRepository.save(warehouseItemsToDelete);
    }
  }

  public Boolean checkWarehouseItemsId(Integer id) {
    return warehouseItemsRepository.existsByIdAndIsDeletedFalse(id);
  }

  public WarehouseItems merge(Integer oldWarehouseItemId, WarehouseItems newWarehouseItem) {
    WarehouseItems oldWarehouseItem = warehouseItemsRepository.findById(oldWarehouseItemId)
        .orElse(null);
    if (oldWarehouseItem == null) {
      return null;
    }
    Optional.ofNullable(newWarehouseItem.getZone_id()).ifPresent(oldWarehouseItem::setZone_id);
    Optional.ofNullable(newWarehouseItem.getItem_id()).ifPresent(oldWarehouseItem::setItem_id);
    Optional.ofNullable(newWarehouseItem.getQuantity()).ifPresent(oldWarehouseItem::setQuantity);

    oldWarehouseItem.setIsdeleted(false);
    return oldWarehouseItem;
  }

  public WarehouseItems update(WarehouseItems mergeWarehouseItem) {
    return warehouseItemsRepository.save(mergeWarehouseItem);
  }

  public void addQuantityToExistWarehouseItem(Integer id, WarehouseItems warehouseItems) {
    WarehouseItems existWarehouseItem = getById(id);
    int existQuantity = existWarehouseItem.getQuantity();
    existWarehouseItem.setQuantity(existQuantity + warehouseItems.getQuantity());
    warehouseItemsRepository.save(existWarehouseItem);
  }

  public List<WarehouseItems> getByProductAndWarehouse(Integer productId,
      Integer warehouseId) {
    return warehouseItemsRepository.findByProductAndWarehouse(productId, warehouseId);
  }

  public int getTotalQuantityByProductIdAndWarehouseId(Integer productId, Integer warehouseId) {
    List<WarehouseItems> warehouseItems = this.getByProductAndWarehouse(productId,
        warehouseId);

//    System.out.println(warehouseItems.toString());
    if (warehouseItems.isEmpty()) {
      return 0;
    }

    int total = 0;
    for (WarehouseItems wi : warehouseItems) {
      total += wi.getQuantity();
    }
    return total;
  }

  public List<OutboundTransactionDetail> createTransactionDetailsByProductAndQuantityAndWarehouse(
      Integer productId, Integer quantity, Integer warehouseId
  ){

    //this list is sorted by expire_date and quantity
    List<WarehouseItems> warehouseItemList = this.getByProductAndWarehouse(productId, warehouseId);
//    System.out.println(warehouseItems.toString());

    List<OutboundTransactionDetail> detailList = new ArrayList<>();

    int leftQuantity = quantity;  //Số lg còn lại cần phải lấy

    for (WarehouseItems warehouseItem : warehouseItemList ){
      OutboundTransactionDetail newDetail = new OutboundTransactionDetail();
      newDetail.setItem_id(warehouseItem.getItem_id());
      newDetail.setZone_id(warehouseItem.getZone_id());

      if (warehouseItem.getQuantity() >= leftQuantity){
        newDetail.setQuantity(leftQuantity);
        detailList.add(newDetail);
        break;
      }

      //if current warehouseItem not enough, get all
      newDetail.setQuantity(warehouseItem.getQuantity()); //Get all
      leftQuantity -= warehouseItem.getQuantity();

      detailList.add(newDetail);
    }

    System.out.println(detailList);

    return detailList;
  }
}
