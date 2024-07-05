package com.app.zware.Repositories;

import com.app.zware.Entities.InternalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternalTransactionRepository extends JpaRepository<InternalTransaction, Integer> {

}
