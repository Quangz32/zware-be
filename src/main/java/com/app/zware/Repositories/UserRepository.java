package com.app.zware.Repositories;

import com.app.zware.Entities.User;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Integer> {

  @Query(value = "select * from users u where u.isdeleted=false", nativeQuery = true)
  List<User> findAll();

  @Query(value = "select * from users u where u.id=?1 and u.isdeleted=false", nativeQuery = true)
  Optional<User> findById(Integer id);

  @Query(value = "select * from users u where u.email=?1 and u.isdeleted=false", nativeQuery = true)
  User findByEmail(String email);

  @Query("select case when COUNT(id)>0 THEN true ELSE false END FROM users u where u.id=?1 and u.isdeleted = false")
  boolean existByIdAndIsDeletedFalse(Integer id);

  @Modifying
  @Query(value = "UPDATE `Users` SET `password`=?2 WHERE id=?1", nativeQuery = true)
  void updatePassword(Integer userId, String hashedPassword);

}

