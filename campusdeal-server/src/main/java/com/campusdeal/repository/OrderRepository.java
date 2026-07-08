package com.campusdeal.repository;

import com.campusdeal.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNo(String orderNo);

    Page<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);

    Page<Order> findByBuyerIdAndStatusOrderByCreatedAtDesc(Long buyerId, String status, Pageable pageable);

    Page<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    Page<Order> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, String status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE (o.buyerId = :userId OR o.sellerId = :userId) ORDER BY o.createdAt DESC")
    Page<Order> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE (o.buyerId = :userId OR o.sellerId = :userId) AND o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByBuyerIdOrSellerIdAndStatusOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("status") String status, Pageable pageable);

    Page<Order> findByStatus(String status, Pageable pageable);

    /** 查找排除特定状态的全部订单（用于排除已删除） */
    Page<Order> findByStatusNot(String status, Pageable pageable);

    long countByStatus(String status);

    long countBySellerIdAndStatus(Long sellerId, String status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'completed'")
    long countCompleted();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'completed'")
    java.math.BigDecimal sumCompletedAmount();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'completed'")
    Optional<java.math.BigDecimal> totalTransactionAmount();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'completed' AND o.completeTime >= :since")
    java.math.BigDecimal sumCompletedAmountSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :since")
    long countSince(@Param("since") LocalDateTime since);

    /** 查找指定状态且在指定时间之前创建的订单（用于自动取消） */
    List<Order> findByStatusAndCreatedAtBefore(String status, LocalDateTime createdAt);
}
