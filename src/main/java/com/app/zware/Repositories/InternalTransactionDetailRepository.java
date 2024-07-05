package com.app.zware.Repositories;

import com.app.zware.Entities.InternalTransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternalTransactionDetailRepository
    extends JpaRepository<InternalTransactionDetail, Integer> {
}
