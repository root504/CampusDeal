package com.campusdeal.repository;

import com.campusdeal.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Page<Announcement> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    List<Announcement> findByStatusOrderByCreatedAtDesc(Integer status);

    List<Announcement> findByStatus(Integer status);

    Page<Announcement> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
