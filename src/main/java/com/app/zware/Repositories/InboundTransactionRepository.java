package com.app.zware.Repositories;

import com.app.zware.Entities.InboundTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InboundTransactionRepository extends JpaRepository<InboundTransaction, Integer> {

  @Query(value = "select * from inboundtransactions where isdeleted=false", nativeQuery = true)
  List<InboundTransaction> findAll();


  @Query(value = "select * from inboundtransactions where id=?1 and isdeleted=false", nativeQuery = true)
  Optional<InboundTransaction> findById(Integer id);

  @Query(value = "select case when COUNT(id)>0 THEN true ELSE false END FROM inboundtransactions where id=?1 and isdeleted = false")
  boolean existByIdAndIsDeletedFalse(Integer id);

  @Query(value = "SELECT * FROM inboundtransactions where warehouse_id=?1 and isdeleted =0",nativeQuery = true)
  List <InboundTransaction> getInboundTransactionById (Integer warehouseId);
}
