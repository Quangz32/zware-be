package com.app.zware.Service;

import com.app.zware.Entities.WarehouseZone;
import com.app.zware.Repositories.WarehouseZoneRespository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WarehouseZoneService {

  @Autowired
  WarehouseZoneRespository warehouseZoneRespository;

  public List<WarehouseZone> getAll() {
    return warehouseZoneRespository.findAll();
  }

  public WarehouseZone createWarehouseZone(WarehouseZone request) {
    request.setIsdeleted(false);
    return warehouseZoneRespository.save(request);
  }

  public WarehouseZone getWarehouseZoneById(Integer id) {
    return warehouseZoneRespository.findById(id)
        .orElse(null);
  }

  public void deleteWarehouseZoneById(Integer id) {
    WarehouseZone warehouseZone = getWarehouseZoneById(id);
    warehouseZone.setIsdeleted(true);
    warehouseZoneRespository.save(warehouseZone);
  }

  public WarehouseZone merger(Integer oldWarehouseZoneId, WarehouseZone newWarehouseZone) {
    WarehouseZone oldWarehouseZone = warehouseZoneRespository.findById(oldWarehouseZoneId)
        .orElse(null);
    if (oldWarehouseZone == null) {
      return null;
    }
    Optional.ofNullable(newWarehouseZone.getName()).ifPresent(oldWarehouseZone::setName);
    Optional.ofNullable(newWarehouseZone.getWarehouse_id())
        .ifPresent(oldWarehouseZone::setWarehouse_id);

    oldWarehouseZone.setIsdeleted(false);

    return oldWarehouseZone;
  }


  public WarehouseZone update(WarehouseZone mergedWarehouseZone) {
    return warehouseZoneRespository.save(mergedWarehouseZone);
  }

  public boolean existById(Integer id){
    return warehouseZoneRespository.existsByIdAndIsDeletedFalse(id);
  }

  public List<WarehouseZone> getByWarehouse(Integer warehouseId){
    return warehouseZoneRespository.findByWarehouseId(warehouseId);
  }

  public WarehouseZone findByIdAndWarehouseId(Integer id,Integer warehouseId){
    return warehouseZoneRespository.findByIdAndWarehouseId(id,warehouseId);
  }
}
