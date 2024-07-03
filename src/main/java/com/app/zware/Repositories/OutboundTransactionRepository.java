package com.app.zware.Repositories;

import com.app.zware.Entities.OutboundTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboundTransactionRepository extends JpaRepository<OutboundTransaction, Integer> {

  @Query(value = "SELECT * FROM outboundtransactions i WHERE i.isdeleted = 0", nativeQuery = true)
  List<OutboundTransaction> findAll();

  @Query(value = "SELECT * FROM outboundtransactions i WHERE i.id = ?1 AND i.isdeleted = 0", nativeQuery = true)
  OutboundTransaction findByOutboundId(Integer id);

  @Query(value = "Select * from outboundtransactions where warehouse_id=?1 AND isdeleted=false", nativeQuery = true)
  List<OutboundTransaction> findByWarehouse(Integer warehouseId);

  @Query("SELECT CASE WHEN COUNT(id) > 0 THEN true ELSE false END FROM outboundtransactions i WHERE i.id = ?1 AND i.isdeleted = false")
  boolean existsByIdAndIsDeletedFalse(Integer id);


}
