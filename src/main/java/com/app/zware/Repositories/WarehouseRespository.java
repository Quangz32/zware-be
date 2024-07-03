package com.app.zware.Repositories;

import com.app.zware.Entities.Warehouse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRespository extends JpaRepository<Warehouse, Integer> {

  @Query(value = "select * from warehouses  where isdeleted=false", nativeQuery = true)
  List<Warehouse> findAll();

  @Query(value = "select * from warehouses where id=?1 and isdeleted=false", nativeQuery = true)
  Optional<Warehouse> findById(Integer id);

  @Query(value = "SELECT * from Warehouses\n"
      + "WHERE id = (SELECT warehouse_id from WarehouseZones where id=?1)", nativeQuery = true)
  Warehouse findByZone(Integer zoneId);

  @Query(value = "select * from warehouses where name=?1 and isdeleted=false", nativeQuery = true)
  Optional<Warehouse> findByName(String name);

  @Query("select case when COUNT(id)>0 THEN true ELSE false END FROM warehouses w where w.id=?1 and w.isdeleted = false")
  boolean existByIdAndIsDeletedFalse(Integer id);


  @Query(value = "SELECT w.* FROM warehouses w INNER JOIN users u ON w.id = u.warehouse_id WHERE u.id = ?1 AND w.isdeleted = false", nativeQuery = true)
  Warehouse findByUserId(Integer userId);

}
