package com.app.zware.Repositories;

import com.app.zware.Entities.InternalTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InternalTransactionRepository extends JpaRepository<InternalTransaction, Integer> {
    @Query(value = "select case when COUNT(id)>0 THEN true ELSE false END FROM internaltransactions where id=?1")
    boolean existByIdAndIsDeletedFalse(Integer id);
}
