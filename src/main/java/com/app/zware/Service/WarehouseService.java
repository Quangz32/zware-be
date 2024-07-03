package com.app.zware.Service;

import com.app.zware.Entities.*;
import com.app.zware.Repositories.*;

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

  @Autowired
  InboundTransactionRepository inboundTransactionRepository;

  @Autowired
  GoodsDisposalRepository goodsDisposalRepository;

  @Autowired
  OutboundTransactionRepository outboundTransactionRepository;


  public List<Warehouse> getWarehouse() {
    return wareHouseRespository.findAll();
  }

  public Warehouse getByZone(Integer zoneId){
    return wareHouseRespository.findByZone(zoneId);
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

  public boolean existById(Integer warehouseId){
    return wareHouseRespository.existByIdAndIsDeletedFalse(warehouseId);
  }




}
