package com.campusdeal.repository;

import com.campusdeal.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Page<User> findByPhoneContainingOrUsernameContaining(String phone, String username, Pageable pageable);
    List<User> findByRole(Integer role);

    /** 清理 users 表中的管理员账号（role=1），不影响 admins 表 */
    @Modifying
    @Transactional
    void deleteByRole(Integer role);

    /** 根据用户名模糊删除，清理 username='管理员' 的记录 */
    @Modifying
    @Transactional
    void deleteByUsername(String username);
}
