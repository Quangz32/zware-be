package com.app.zware.Service;

import com.app.zware.Entities.Warehouse;
import com.app.zware.Entities.WarehouseItems;
import com.app.zware.Entities.WarehouseZone;
import com.app.zware.Repositories.WarehouseItemsRepository;
import com.app.zware.Repositories.WarehouseRespository;
import com.app.zware.Repositories.WarehouseZoneRespository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WarehouseService {

  @Autowired
  WarehouseRespository wareHouseRespository;

  @Autowired
  WarehouseZoneRespository zoneRespository;

  @Autowired
  WarehouseItemsRepository warehouseItemsRepository;


  public List<Warehouse> getWarehouse() {
    return wareHouseRespository.findAll();
  }

  public Warehouse createWareHouse(Warehouse request) {
    Warehouse warehouse = new Warehouse();
    warehouse.setName(request.getName());
    warehouse.setAddress(request.getAddress());
    return wareHouseRespository.save(warehouse);
  }

  public Warehouse getWareHouseById(int id) {
    return wareHouseRespository.findById(id).orElse(null);

  }

  public void deleteWareHouseById(int id) {

    Warehouse warehouse = getWareHouseById(id);
    warehouse.setIsdeleted(true);
    wareHouseRespository.save(warehouse);

    //wareHouseRespository.deleteById(id);
  }

  public Warehouse merge(Integer oldWarehouseId, Warehouse newWarehouse) {
    Warehouse oldWarehouse = wareHouseRespository.findById(oldWarehouseId).orElse(null);
    if (oldWarehouse == null) {
      return null;
    }

    Optional.ofNullable(newWarehouse.getName()).ifPresent(oldWarehouse::setName);
    Optional.ofNullable(newWarehouse.getAddress()).ifPresent(oldWarehouse::setAddress);

    return oldWarehouse;
  }


  public Warehouse updateWarehouse(Warehouse mergedWarehouse) {
    return wareHouseRespository.save(mergedWarehouse);

  }

  public List<WarehouseZone> getZonesByWarehouseId(Integer warehouseId) {
    return zoneRespository.findByWarehouseId(warehouseId);
  }

  public List<WarehouseItems> getItemsByWarehouseId(Integer warehouseId) {
    return warehouseItemsRepository.findWarehouseItemByWarehouseId(warehouseId);
  }

  public boolean existById(Integer warehouseId){
    return wareHouseRespository.existByIdAndIsDeletedFalse(warehouseId);
  }

}
