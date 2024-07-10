package com.app.zware.Repositories;

import com.app.zware.Entities.InternalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternalTransactionRepository extends JpaRepository<InternalTransaction, Integer> {
    @Query(value = "select case when COUNT(id)>0 THEN true ELSE false END FROM internaltransactions where id=?1")
    boolean existByIdAndIsDeletedFalse(Integer id);

//    @Query(value = "SELECT * FROM internaltransactions WHERE (source_warehouse = ?1 OR destination_warehouse = ?1)", nativeQuery = true)
//    List<InternalTransaction> findByWarehouse(Integer warehouseId);
    @Query(value = "SELECT * FROM internaltransactions WHERE (type = 'inbound' AND destination_warehouse = ?1) OR (type = 'outbound' AND source_warehouse = ?1)", nativeQuery = true)
    List<InternalTransaction> findByWarehouse(Integer warehouseId);

    @Query(value = "SELECT * FROM internaltransactions WHERE type = 'outbound' AND destination_warehouse = ?1 AND status = 'pending'", nativeQuery = true)
    List<InternalTransaction> findByDestinationId(Integer destinationId);

    @Query(value = "SELECT * FROM internaltransactions WHERE type = 'outbound' AND status = 'pending'", nativeQuery = true)
    List<InternalTransaction> findAllInboundInternal();
}
