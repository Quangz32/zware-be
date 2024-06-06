package com.app.zware.Service;

import com.app.zware.Entities.WarehouseItems;
import com.app.zware.Repositories.WarehouseItemsRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WarehouseItemsService {

  @Autowired
  WarehouseItemsRepository warehouseItemsRepository;

  public List<WarehouseItems> getAllWarehouseItems() {
    return warehouseItemsRepository.findAll();
  }

  public WarehouseItems getById(int id) {
    return warehouseItemsRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Not Found WareHouseItems"));
  }

  public WarehouseItems createWarehouseItems(WarehouseItems request) {
    return warehouseItemsRepository.save(request);
  }

  public void deleteWarehouseItemsById(int id) {
    warehouseItemsRepository.deleteById(id);
  }

  public Boolean checkWarehouseItemsId(int id) {
    return warehouseItemsRepository.existsById(id);
  }

  public WarehouseItems merger(Integer oldWarehouseItemId,WarehouseItems newWarehouseItem){
    WarehouseItems oldWarehouseItem =  warehouseItemsRepository.findById(oldWarehouseItemId).orElse(null);
    if(oldWarehouseItem==null){
      return null;
    }
    Optional.ofNullable(newWarehouseItem.getZone_id()).ifPresent(oldWarehouseItem::setZone_id);
    Optional.ofNullable(newWarehouseItem.getItem_id()).ifPresent(oldWarehouseItem::setItem_id);
    Optional.ofNullable(newWarehouseItem.getQuantity()).ifPresent(oldWarehouseItem::setQuantity);

  return oldWarehouseItem;
  }
  public WarehouseItems update (WarehouseItems mergeWarehouseItem){
    return warehouseItemsRepository.save(mergeWarehouseItem);
  }

}
