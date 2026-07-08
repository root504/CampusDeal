package com.campusdeal.repository;

import com.campusdeal.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
    Optional<Admin> findByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
}
