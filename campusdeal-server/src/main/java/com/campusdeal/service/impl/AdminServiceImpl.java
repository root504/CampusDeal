package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.common.PageResult;
import com.campusdeal.entity.*;
import com.campusdeal.repository.*;
import com.campusdeal.service.AdminService;
import com.campusdeal.service.MessageService;
import com.campusdeal.service.SensitiveWordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AnnouncementRepository announcementRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;
    private final SensitiveWordService sensitiveWordService;
    public AdminServiceImpl(UserRepository userRepository, ProductRepository productRepository, OrderRepository orderRepository,
                            AnnouncementRepository announcementRepository, AdminRepository adminRepository,
                            PasswordEncoder passwordEncoder, MessageService messageService,
                            SensitiveWordService sensitiveWordService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.announcementRepository = announcementRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageService = messageService;
        this.sensitiveWordService = sensitiveWordService;
    }


    @Override
    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userRepository.count());
        data.put("totalProducts", productRepository.count());
        data.put("totalOrders", orderRepository.count());
        data.put("totalAmount", orderRepository.totalTransactionAmount().orElse(BigDecimal.ZERO));
        return data;
    }

    @Override
    public Object getTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -6);
        for (int i = 0; i < 7; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", String.format("%tF", cal));
            day.put("count", 0);
            trend.add(day);
            cal.add(Calendar.DATE, 1);
        }
        return trend;
    }

    @Override
    public Object getCategoryDistribution() {
        List<Object[]> raw = productRepository.categoryDistribution();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryId", row[0]);
            item.put("name", row[1]);
            item.put("count", row[2]);
            result.add(item);
        }
        return result;
    }

    @Override
    public PageResult<User> getUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;
        if (keyword == null || keyword.isBlank()) {
            userPage = userRepository.findAll(pageable);
        } else {
            userPage = userRepository.findByPhoneContainingOrUsernameContaining(keyword, keyword, pageable);
        }
        return PageResult.of(userPage.getContent(), userPage.getTotalElements(), page, size);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setStatus(status);
        userRepository.save(user);
    }

    @Override
    public void editUser(Long userId, String nickname, String phone, String school, String campus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        // 超级管理员不可编辑
        if ("13900000000".equals(user.getPhone())) {
            throw new BusinessException("超级管理员不可编辑");
        }
        if (nickname != null && !nickname.isBlank()) {
            user.setUsername(nickname);
        }
        if (phone != null && !phone.isBlank()) {
            // 检查手机号是否被其他用户占用
            User existing = userRepository.findByPhone(phone).orElse(null);
            if (existing != null && !existing.getId().equals(userId)) {
                throw new BusinessException("手机号已被占用");
            }
            user.setPhone(phone);
        }
        if (school != null) user.setSchool(school);
        if (campus != null) user.setCampus(campus);
        userRepository.save(user);
    }

    @Override
    public void updateUserRole(Long userId, Integer role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if ("13900000000".equals(user.getPhone())) {
            throw new BusinessException("超级管理员权限不可修改");
        }

        Integer oldRole = user.getRole();
        user.setRole(role);
        userRepository.save(user);

        // 同步 admins 表：提升为管理员
        if (role == 1 && oldRole != 1) {
            if (!adminRepository.existsByPhone(user.getPhone())) {
                Admin admin = Admin.builder()
                        .username(user.getPhone())
                        .phone(user.getPhone())
                        .passwordHash(user.getPasswordHash())
                        .name(user.getUsername())
                        .role("admin")
                        .status(1)
                        .build();
                adminRepository.save(admin);
            }
        }

        // 同步 admins 表：降级为普通用户
        if (role == 0 && oldRole != 0) {
            adminRepository.findByPhone(user.getPhone()).ifPresent(admin -> {
                adminRepository.delete(admin);
            });
        }
    }

    @Override
    public PageResult<Announcement> getAnnouncements(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Announcement> aPage = announcementRepository.findAll(pageable);
        return PageResult.of(aPage.getContent(), aPage.getTotalElements(), page, size);
    }

    @Override
    public Announcement createAnnouncement(Long adminId, String title, String content) {
        // 敏感词检测
        sensitiveWordService.checkContent(title);
        sensitiveWordService.checkContent(content);

        Announcement a = Announcement.builder()
                .adminId(adminId)
                .title(title)
                .content(content)
                .status(1)
                .build();
        a = announcementRepository.save(a);

        // 广播给所有普通用户
        List<User> allUsers = userRepository.findByRole(0);
        if (allUsers.isEmpty()) {
            return a;
        }
        String msgContent = "【系统公告】" + title + "\n" + content;
        int successCount = 0;
        int failCount = 0;
        for (User u : allUsers) {
            try {
                Message msg = messageService.sendSystemMessage(u.getId(), msgContent, "announcement", a.getId());
                if (msg != null) {
                    successCount++;
                }
            } catch (Exception e) {
                failCount++;
            }
        }

        return a;
    }

    @Override
    public Announcement updateAnnouncement(Long id, String title, String content) {
        // 敏感词检测
        if (title != null) sensitiveWordService.checkContent(title);
        if (content != null) sensitiveWordService.checkContent(content);

        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
        if (title != null) a.setTitle(title);
        if (content != null) a.setContent(content);
        return announcementRepository.save(a);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new BusinessException("公告不存在");
        }
        announcementRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> syncAnnouncementMessages() {
        List<Announcement> announcements = announcementRepository.findByStatus(1);
        List<User> users = userRepository.findByRole(0);

        int totalAnnouncements = announcements.size();
        int totalUsers = users.size();
        int created = 0;
        int skipped = 0;

        for (Announcement a : announcements) {
            String msgContent = "【系统公告】" + a.getTitle() + "\n" + a.getContent();
            for (User u : users) {
                Message msg = messageService.sendSystemMessage(u.getId(), msgContent, "announcement", a.getId());
                if (msg != null) {
                    created++;
                } else {
                    skipped++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalAnnouncements", totalAnnouncements);
        result.put("totalUsers", totalUsers);
        result.put("created", created);
        result.put("skipped", skipped);
        return result;
    }
}
