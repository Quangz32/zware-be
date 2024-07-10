package com.app.zware.Repositories;

import com.app.zware.Entities.History;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {

  @Query(value = "SELECT * FROM `WarehouseHistories`\n"
      + "WHERE warehouse_id=?1 and product_id=?2", nativeQuery = true)
  List<History> findByWarehouseAndProduct(Integer warehouseId, Integer productId);

  @Query(value = "SELECT * FROM `WarehouseHistories`\n"
      + "WHERE warehouse_id=:warehouseId and product_id=:productId \n"
      + "AND date >= :startDate and date <= :endDate\n"
      + "ORDER BY date, id", nativeQuery = true)
  List<History> findByWarehouseAndProductAndDateInterval(
      @Param("warehouseId") Integer warehouseId,
      @Param("productId") Integer productId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
