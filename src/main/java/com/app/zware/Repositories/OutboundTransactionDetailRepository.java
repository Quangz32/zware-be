package com.app.zware.Repositories;

import com.app.zware.Entities.OutboundTransactionDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboundTransactionDetailRepository extends
    JpaRepository<OutboundTransactionDetail, Integer> {

  @Query(value =
      "SELECT otd.id, otd.transaction_id, otd.item_id, otd.quantity, otd.zone_id FROM outboundtransactiondetails otd JOIN"
          +
          " outboundtransactions ot ON otd.transaction_id = ot.id WHERE ot.isdeleted = false AND ot.id = ?1", nativeQuery = true)
  List<OutboundTransactionDetail> findAllDetailsByOutboundTransactionIdAndIsDeletedFalse(Integer outboundTransactionId);

  @Query(value =
          "SELECT * FROM `outboundtransactiondetails` WHERE transaction_id = ?1", nativeQuery = true)
  List<OutboundTransactionDetail> findDetailsAndIsDeletedFalse(Integer outboundTransactionId);

}
