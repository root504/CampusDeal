package com.campusdeal.repository;

import com.campusdeal.entity.UserCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCreditRepository extends JpaRepository<UserCredit, Long> {
    Optional<UserCredit> findByUserId(Long userId);
}
