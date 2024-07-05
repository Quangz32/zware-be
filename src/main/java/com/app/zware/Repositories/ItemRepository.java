package com.app.zware.Repositories;

import com.app.zware.Entities.Item;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Integer> {

  @Query(value = "SELECT * FROM items i WHERE i.name = ?1 AND i.isdeleted = 0", nativeQuery = true)
  Optional<Item> findByName(String name);

  @Query(value = "SELECT * FROM items i WHERE i.isdeleted = 0", nativeQuery = true)
  List<Item> findAll();

  @Query(value = "SELECT * FROM items i WHERE i.id = ?1 AND i.isdeleted = 0", nativeQuery = true)
  Optional<Item> findById(Integer id);

  @Query(value = "SELECT * FROM items i WHERE i.product_id = ?1 AND i.isdeleted = 0", nativeQuery = true)
  List<Item> findByProductId(Integer id);



  @Query("SELECT CASE WHEN COUNT(id) > 0 THEN true ELSE false END FROM items i WHERE i.id = ?1 AND i.isdeleted = false")
  boolean existsByIdAndIsDeletedFalse(Integer id);

  @Query(value = "SELECT * FROM items i WHERE i.product_id = :productId AND i.expire_date = :expiredDate AND i.isdeleted = false", nativeQuery = true)
  Item findByProductIdAndExpiredDate(@Param("productId") Integer productId,
      @Param("expiredDate") LocalDate expiredDate);
}
