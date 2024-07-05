package com.app.zware.Repositories;

import com.app.zware.Entities.InternalTransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InternalTransactionDetailRepository
    extends JpaRepository<InternalTransactionDetail, Integer> {

    @Query(value = "select * from internaltransactiondetails where transaction_id=?1",nativeQuery = true)
    List<InternalTransactionDetail> findByTransactionId(Integer id);
}
