package com.campusdeal.repository;

import com.campusdeal.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    Optional<Review> findByOrderIdAndBuyerId(Long orderId, Long buyerId);

    List<Review> findByProductId(Long productId);

    boolean existsByOrderIdAndBuyerId(Long orderId, Long buyerId);

    /** 统计某卖家收到的评价总人数（去重 buyerId） */
    @Query("SELECT COUNT(DISTINCT r.buyerId) FROM Review r WHERE r.sellerId = :sellerId")
    long countDistinctBuyersBySellerId(@Param("sellerId") Long sellerId);
}
