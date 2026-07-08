package com.campusdeal.repository;

import com.campusdeal.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    List<CartItem> findByUserIdAndSelected(Long userId, Integer selected);

    @Modifying
    @Query("UPDATE CartItem c SET c.selected = :selected WHERE c.userId = :userId")
    void updateSelectAll(@Param("userId") Long userId, @Param("selected") Integer selected);

    void deleteByUserIdAndProductIdIn(Long userId, List<Long> productIds);

    void deleteByProductId(Long productId);

    long countByUserId(Long userId);
}
