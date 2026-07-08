package com.campusdeal.repository;

import com.campusdeal.entity.OrderRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRuleRepository extends JpaRepository<OrderRule, Long> {
    List<OrderRule> findAllByOrderByUpdatedAtDesc();
    List<OrderRule> findByAppliedTrue();

    @Modifying
    @Query("UPDATE OrderRule r SET r.applied = :applied WHERE r.id = :id")
    int setApplied(Long id, Boolean applied);
}
