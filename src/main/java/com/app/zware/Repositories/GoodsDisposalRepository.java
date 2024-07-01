package com.app.zware.Repositories;

import com.app.zware.Entities.GoodsDisposal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsDisposalRepository extends JpaRepository<GoodsDisposal, Integer> {

  @Query(value = "select * from goodsdisposal where isdeleted=false", nativeQuery = true)
  List<GoodsDisposal> findAll();


  @Query(value = "select * from goodsdisposal where id=?1 and isdeleted=false", nativeQuery = true)
  Optional<GoodsDisposal> findById(Integer id);


  @Query("select case when COUNT(id)>0 THEN true ELSE false END FROM goodsdisposal g where g.id=?1 and g.isdeleted = false")
  boolean existByIdAndIsDeletedFalse(Integer id);

  @Query(value = "SELECT * FROM goodsdisposal WHERE warehouse_id=?1 and isdeleted=0",nativeQuery = true)
  List<GoodsDisposal> findByWarehouse(Integer warehouseId);
}
