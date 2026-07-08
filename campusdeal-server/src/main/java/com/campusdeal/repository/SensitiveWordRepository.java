package com.campusdeal.repository;

import com.campusdeal.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long> {
    List<SensitiveWord> findAllByOrderByUpdatedAtDesc();
    List<SensitiveWord> findByAppliedTrue();
    boolean existsByWord(String word);
}
