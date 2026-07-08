package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.dto.request.LoginRequest;
import com.campusdeal.dto.request.RegisterRequest;
import com.campusdeal.dto.response.LoginResponse;
import com.campusdeal.dto.response.UserProfileResponse;
import com.campusdeal.entity.User;
import com.campusdeal.repository.AnnouncementRepository;
import com.campusdeal.repository.UserCreditRepository;
import com.campusdeal.repository.UserRepository;
import com.campusdeal.service.AuthService;
import com.campusdeal.service.MessageService;
import com.campusdeal.service.SystemLogService;
import com.campusdeal.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SystemLogService systemLogService;
    private final HttpServletRequest request;
    private final AnnouncementRepository announcementRepository;
    private final MessageService messageService;
    private final UserCreditRepository userCreditRepository;
    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder,
                           SystemLogService systemLogService, HttpServletRequest request,
                           AnnouncementRepository announcementRepository, MessageService messageService,
                           UserCreditRepository userCreditRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.systemLogService = systemLogService;
        this.request = request;
        this.announcementRepository = announcementRepository;
        this.messageService = messageService;
        this.userCreditRepository = userCreditRepository;
    }


    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("该手机号已注册");
        }

        User user = User.builder()
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername() != null ? request.getUsername() : "用户" + request.getPhone().substring(7))
                .school(request.getSchool())
                .campus(request.getCampus())
                .role(0)
                .build();

        user = userRepository.save(user);

        // 为新注册用户创建默认信誉分记录
        com.campusdeal.entity.UserCredit userCredit = new com.campusdeal.entity.UserCredit();
        userCredit.setUserId(user.getId());
        userCredit.setCurrentScore(100);
        userCreditRepository.save(userCredit);

        // 自动推送已发布公告给新用户（单个公告发送失败不影响其他公告和注册流程）
        List<com.campusdeal.entity.Announcement> announcements =
                announcementRepository.findByStatus(1);
        for (com.campusdeal.entity.Announcement announcement : announcements) {
            try {
                String msgContent = "【系统公告】" + announcement.getTitle() + "\n" + announcement.getContent();
                messageService.sendSystemMessage(
                        user.getId(),
                        msgContent,
                        "announcement",
                        announcement.getId()
                );
            } catch (Exception e) {
                // 单个公告发送失败不阻断注册流程
            }
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());

        return buildLoginResponse(user, accessToken, refreshToken);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new BusinessException("手机号或密码错误"));

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被封禁");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("手机号或密码错误");
        }

        // 管理员账号只能通过后台登录入口
        if (!"admin".equals(request.getLoginType()) && user.getRole() == 1) {
            throw new BusinessException("该账号为管理员账号，请使用后台登录入口");
        }

        // 管理员登录入口：验证账号是否有管理员权限
        if ("admin".equals(request.getLoginType()) && user.getRole() != 1) {
            throw new BusinessException("该账号无管理员权限");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());

        // 记录登录日志
        String ip = getClientIp();
        String roleLabel = user.getRole() == 1 ? "管理员" : "用户";
        systemLogService.saveLog("用户登录", roleLabel + " " + user.getPhone() + "(" + user.getUsername() + ") 登录成功", user.getUsername(), ip);

        return buildLoginResponse(user, accessToken, refreshToken);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(401, "Token已过期，请重新登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被封禁，请联系管理员");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());

        return buildLoginResponse(user, newAccessToken, newRefreshToken);
    }

    @Override
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(Long userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getSchool() != null) user.setSchool(request.getSchool());
        if (request.getCampus() != null) user.setCampus(request.getCampus());

        user = userRepository.save(user);
        return toProfileResponse(user);
    }

    @Override
    public void logout(String accessToken) {
        // Token 黑名单逻辑可在接入 Redis 后实现
    }

    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .userId(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .school(user.getSchool())
                .college(user.getCollege())
                .campus(user.getCampus())
                .grade(user.getGrade())
                .creditScore(user.getCreditScore())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .username(user.getUsername())
                .avatarUrl(user.getAvatarUrl())
                .school(user.getSchool())
                .college(user.getCollege())
                .campus(user.getCampus())
                .grade(user.getGrade())
                .creditScore(user.getCreditScore())
                .isVerified(user.getIsVerified())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
