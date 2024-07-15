package com.app.zware.Repositories;

import com.app.zware.Entities.WarehouseItems;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseItemsRepository extends JpaRepository<WarehouseItems, Integer> {
//    Optional<WarehouseItems> findByZoneIdAndItemId(Integer zone_id, Integer item_id);
//    @Query(value = "SELECT w FROM warehouseitems w WHERE w.zone_id = :zoneId and w.item_id = :itemId and w.isdeleted = 0")
//    WarehouseItems findByZoneIdAndItemId(@Param("zoneId") Integer zoneId, @Param("itemId") Integer itemId);

  @Query(value = "SELECT * FROM warehouseitems w WHERE w.zone_id = ?1 and w.item_id = ?2 and w.isdeleted = 0", nativeQuery = true)
  WarehouseItems findByZoneIdAndItemId(Integer zoneId, Integer itemId);

  @Query(value = "SELECT * FROM warehouseitems i WHERE i.isdeleted = 0", nativeQuery = true)
  List<WarehouseItems> findAll();

  @Query(value = "SELECT * FROM warehouseitems i WHERE i.id = ?1 AND i.isdeleted = 0", nativeQuery = true)
  Optional<WarehouseItems> findById(Integer id);

  @Query(value = "SELECT * FROM warehouseitems i WHERE i.zone_id = ?1 AND i.isdeleted = 0", nativeQuery = true)
  List<WarehouseItems> findZoneId(Integer id);

  @Query("SELECT CASE WHEN COUNT(id) > 0 THEN true ELSE false END FROM warehouseitems i WHERE i.id = ?1 AND i.isdeleted = false")
  boolean existsByIdAndIsDeletedFalse(Integer id);

//    @Query(value = "SELECT * FROM warehouseitems i WHERE i.zone_id = ?1 AND i.item_id = ?2 AND i.isdeleted = false", nativeQuery = true)
//    WarehouseItems findByZoneIdAndItemId(Integer zoneId, Integer itemId);


  @Query(value = "SELECT wi.* " +
      "FROM WarehouseItems wi " +
      "JOIN WarehouseZones wz ON wi.zone_id = wz.id " +
      "WHERE wz.warehouse_id = :warehouseId and wi.isdeleted=false", nativeQuery = true)
  List<WarehouseItems> findWarehouseItemByWarehouseId(Integer warehouseId);


  @Query(value = "SELECT COUNT(wi.id) FROM WarehouseItems wi "
      + "JOIN WarehouseZones wz ON wi.zone_id = wz.id "
      + "WHERE wz.warehouse_id = :warehouseId and wi.isdeleted=false", nativeQuery = true)
  Long countItemsInWarehouse(Integer warehouseId);


  @Query(value = "SELECT wi.* FROM WarehouseItems wi "
      + "JOIN WarehouseZones wz ON wi.zone_id = wz.id "
      + "WHERE wi.zone_id = :zoneId AND wi.quantity > 0 AND wi.isdeleted = false", nativeQuery = true)
  List<WarehouseItems> findItemsInWarehouseZone(Integer zoneId);

  @Query(value = "SELECT * FROM WarehouseItems wi "
      + "WHERE wi.item_id = :id AND wi.quantity > 0 AND wi.isdeleted = false", nativeQuery = true)
  List<WarehouseItems> findByItemId(Integer id);

  @Query(value = "select wi.* from \n"
      + "WarehouseItems wi \n"
      + "JOIN Items i on i.id = wi.item_id \n"
      + "Join WarehouseZones wz on wz.id = wi.zone_id \n"
      + "where wz.warehouse_id = ?2 and i.product_id=?1\n"
      + "ORDER BY i.expire_date, wi.quantity DESC;", nativeQuery = true)
  List<WarehouseItems> findByProductAndWarehouse(Integer productId, Integer warehouseId);

  @Query(value = "SELECT wi.* from \n"
      + "WarehouseItems wi \n"
      + "JOIN Items i on i.id = wi.item_id \n"
      + "JOIN WarehouseZones wz on wz.id = wi.zone_id \n"
      + "WHERE wz.warehouse_id = ?2 and i.product_id=?1\n"
      + "AND  i.expire_date > CURDATE()\n"
      + "ORDER BY i.expire_date, wi.quantity DESC;", nativeQuery = true)
  List<WarehouseItems> findNonExpiredByProductAndWarehouse(Integer productId, Integer warehouseId);

  @Query(value = "select wi.* from warehouseitems wi\n"
      + "join items i on i.id = wi.item_id\n"
      + "where wi.zone_id=?1 and wi.isdeleted=false\n"
      + "order by i.product_id, i.expire_date", nativeQuery = true)
  List<WarehouseItems> findByZoneId(Integer zoneId);


  @Query(value = "select wi.* from WarehouseItems wi " +
      "JOIN Items i on i.id = wi.item_id " +
      "where i.product_id = ?1 " +
      "ORDER BY i.expire_date, wi.quantity DESC", nativeQuery = true)
  List<WarehouseItems> findByProductId(Integer productId);

  @Query(value = "SELECT wi.* from WarehouseItems wi \n"
      + "JOIN Items i on i.id = wi.item_id\n"
      + "WHERE wi.zone_id = ?1\n"
      + "AND i.product_id = ?2\n"
      + "AND i.expire_date= ?3\n"
      + "LIMIT 1", nativeQuery = true)
  WarehouseItems findByZoneAndProductAndDate(Integer zoneId, Integer productId, LocalDate date);

  @Query(value = "SELECT COALESCE(SUM(wi.quantity), 0) FROM warehouseitems wi JOIN items i ON wi.item_id = i.id \n" +
          "          JOIN warehousezones wz ON wz.id = wi.zone_id\n" +
          "          WHERE i.product_id = ?1 AND wz.warehouse_id= ?2 AND i.expire_date > CURRENT_DATE AND wi.isdeleted = false AND i.isdeleted = false", nativeQuery = true)
  Integer findTotalQuantityByProductIdAndWarehouse(Integer productId, Integer warehouseId);

  // List ExpiredItem by Warehouse
  @Query(value = " Select wi.* from WarehouseItems wi " +
                 "JOIN Items i on i.id = wi.item_id " +
                 "JOIN WarehouseZones wz on wi.zone_id = wz.id " +
                 "JOIN Warehouses w on w.id = wz.warehouse_id  " +
                 "where i.expire_date < CURRENT_DATE() and w.id = ?1 and wi.isdeleted = false",nativeQuery = true)
  List<WarehouseItems> findExpiredByWarehouse(Integer warehouseId);
}
