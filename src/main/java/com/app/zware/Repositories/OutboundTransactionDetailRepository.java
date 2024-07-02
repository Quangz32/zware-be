package com.app.zware.Repositories;

import com.app.zware.Entities.OutboundTransactionDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboundTransactionDetailRepository extends
    JpaRepository<OutboundTransactionDetail, Integer> {

  @Query(value =
      "SELECT * from outboundtransactiondetails\n" +
      "WHERE transaction_id = ?1",
      nativeQuery = true)
  List<OutboundTransactionDetail> findByOutboundTransaction(Integer transactionId);
}
