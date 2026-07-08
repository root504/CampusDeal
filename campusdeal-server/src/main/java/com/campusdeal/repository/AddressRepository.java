package com.campusdeal.repository;

import com.campusdeal.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = 0 WHERE a.userId = :userId")
    void clearDefaultByUserId(Long userId);
}
