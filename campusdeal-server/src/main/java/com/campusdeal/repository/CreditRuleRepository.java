package com.campusdeal.repository;

import com.campusdeal.entity.CreditRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditRuleRepository extends JpaRepository<CreditRule, Long> {
    List<CreditRule> findAllByOrderBySortOrderAsc();
    List<CreditRule> findByAppliedTrueOrderBySortOrderAsc();

    @Modifying
    @Query("UPDATE CreditRule r SET r.applied = :applied WHERE r.id = :id")
    int setApplied(Long id, Boolean applied);
}
