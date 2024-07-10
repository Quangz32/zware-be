package com.app.zware.Repositories;

import com.app.zware.Entities.WarehouseZone;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseZoneRespository extends JpaRepository<WarehouseZone, Integer> {
  @Query(value = "SELECT * FROM warehousezones i WHERE i.isdeleted = 0", nativeQuery = true)
  List<WarehouseZone> findAll();

  @Query(value = "SELECT * FROM warehousezones i WHERE i.id = ?1 AND i.isdeleted = 0", nativeQuery = true)
  Optional<WarehouseZone> findById(Integer id);

  @Query(value = "SELECT * FROM warehousezones i WHERE i.name =?1 AND i.isdeleted = 0", nativeQuery = true)
  List<WarehouseZone> findByName(String name);

  @Query(
      value = "SELECT * FROM warehousezones z WHERE z.warehouse_id = ?1 AND z.isdeleted = 0",
      nativeQuery = true)
  List<WarehouseZone> findByWarehouseId(Integer warehouseId);

  @Query(value="SELECT * FROM warehousezones where name=?1 and warehouse_id=?2 and isdeleted = false LIMIT 1", nativeQuery = true)
  WarehouseZone findByNameAndWarehouseId(String name, Integer warehouseId);

  @Query("SELECT CASE WHEN COUNT(id) > 0 THEN true ELSE false END FROM warehousezones i WHERE i.id = ?1 AND i.isdeleted = false")
  boolean existsByIdAndIsDeletedFalse(Integer id);


  @Query(value = "select count(*) from warehousezones where warehouse_id=?1 and isdeleted=0",nativeQuery = true)
  Long countZoneInWarehouse(Integer warehouseId);

  @Query(value = "SELECT * FROM warehousezones  WHERE id = ?1 AND warehouse_id = ?2 AND isdeleted = false", nativeQuery = true)
  WarehouseZone findByIdAndWarehouseId(Integer id, Integer warehouseId);

}
