package com.campusdeal.repository;

import com.campusdeal.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByStatusOrderBySortOrderAsc(Integer status);

    List<Banner> findAllByOrderBySortOrderAsc();
}
