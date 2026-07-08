package com.campusdeal.repository;

import com.campusdeal.entity.Category;
import com.campusdeal.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatus(Integer status, Pageable pageable);

    Page<Product> findByStatusNot(Integer status, Pageable pageable);

    Page<Product> findByCategoryIdAndStatus(Integer categoryId, Integer status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = :status AND " +
           "(p.title LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    List<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    Page<Product> findBySellerId(Long sellerId, Pageable pageable);

    Page<Product> findBySellerIdAndStatus(Long sellerId, Integer status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.categoryId = :categoryId AND p.status = :status AND p.id <> :excludeId " +
           "ORDER BY p.favoriteCount DESC")
    List<Product> findSimilarProducts(@Param("categoryId") Integer categoryId,
                                       @Param("status") Integer status,
                                       @Param("excludeId") Long excludeId,
                                       Pageable pageable);

    long countByStatus(Integer status);

    long countBySellerId(Long sellerId);

    long countBySellerIdAndStatus(Long sellerId, Integer status);

    @Query("SELECT p.categoryId, c.name, COUNT(p) FROM Product p JOIN Category c ON p.categoryId = c.id " +
           "WHERE p.status = 1 GROUP BY p.categoryId, c.name ORDER BY p.categoryId")
    List<Object[]> categoryDistribution();
}
