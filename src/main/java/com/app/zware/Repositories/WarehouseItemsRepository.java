package com.app.zware.Repositories;

import com.app.zware.Entities.WarehouseItems;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Truy vấn lấy item theo ngày hết hạn gần nhất và đủ quantity
    @Query(value = "SELECT wi.* FROM warehouseitems wi " +
            "JOIN warehousezones wz ON wi.zone_id = wz.id " +
            "JOIN items i ON wi.item_id = i.id " +
            "WHERE i.product_id = :productId AND wz.warehouse_id = :warehouseId " +
            "AND wi.isdeleted = false AND wi.quantity > 0  " +
            "AND wi.isdeleted = false AND i.isdeleted = false " +
            "AND (i.expire_date IS NULL OR i.expire_date > CURDATE())"+
            "ORDER BY i.expire_date ASC, wi.quantity DESC", nativeQuery = true)
    List<WarehouseItems> findNearestExpiryItems(@Param("productId") Integer productId, @Param("warehouseId") Integer warehouseId);


    @Query(value = "WITH TotalQuantity AS (\n" +
            "    SELECT SUM(wi.quantity) AS totalQuantity\n" +
            "    FROM WarehouseItems wi\n" +
            "    INNER JOIN Items i ON wi.item_id = i.id\n" +
            "    INNER JOIN WarehouseZones wz ON wi.zone_id = wz.id\n" +
            "    WHERE wz.warehouse_id = :warehouseId AND i.product_id = :productId\n" +
            "),\n" +
            "ProcessingQuantity AS (\n" +
            "    SELECT COALESCE(SUM(otd.quantity), 0) AS Processing\n" +
            "    FROM OutboundTransactionDetails otd\n" +
            "    INNER JOIN OutboundTransactions ot ON otd.transaction_id = ot.id\n" +
            "    INNER JOIN Items i ON otd.item_id = i.id\n" +
            "    WHERE (ot.status = 'pending' OR ot.status = 'processing')\n" +
            "    AND ot.warehouse_id = :warehouseId AND i.product_id = :productId\n" +
            ")\n" +
            "SELECT tq.totalQuantity - pq.Processing AS AvailableQuantity\n" +
            "FROM TotalQuantity tq, ProcessingQuantity pq;", nativeQuery = true)
    Integer sumQuantityByProductIdAndWarehouseId(@Param("productId") Integer productId, @Param("warehouseId") Integer warehouseId);
}
