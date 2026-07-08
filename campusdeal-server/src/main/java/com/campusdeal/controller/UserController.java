package com.campusdeal.controller;

import com.campusdeal.common.BusinessException;
import com.campusdeal.common.Result;
import com.campusdeal.dto.request.ChangePasswordRequest;
import com.campusdeal.dto.request.UpdateProfileRequest;
import com.campusdeal.dto.response.UserProfileResponse;
import com.campusdeal.dto.response.UserStatsResponse;
import com.campusdeal.entity.User;
import com.campusdeal.repository.OrderRepository;
import com.campusdeal.repository.ProductRepository;
import com.campusdeal.repository.UserRepository;
import com.campusdeal.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, ProductRepository productRepository,
                          OrderRepository orderRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    private Long getUserId(String authHeader) {
        return jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
    }

    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(Result::success)
                .orElse(Result.error(404, "用户不存在"));
    }

    @PutMapping("/profile")
    public Result<UserProfileResponse> updateProfile(@RequestHeader("Authorization") String authHeader,
                                                      @RequestBody UpdateProfileRequest request) {
        Long userId = getUserId(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getSchool() != null) user.setSchool(request.getSchool());
        if (request.getCollege() != null) user.setCollege(request.getCollege());
        if (request.getCampus() != null) user.setCampus(request.getCampus());
        if (request.getGrade() != null) user.setGrade(request.getGrade());

        user = userRepository.save(user);
        return Result.success(toProfileResponse(user));
    }

    @PutMapping("/password")
    public Result<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                     @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getUserId(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            return Result.error("旧密码错误");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return Result.success("密码修改成功", null);
    }

    @GetMapping("/stats")
    public Result<UserStatsResponse> getStats(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        long publishedCount = productRepository.countBySellerId(userId);
        long soldCount = orderRepository.countBySellerIdAndStatus(userId, "completed");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return Result.success(new UserStatsResponse(publishedCount, soldCount,
                user.getCreditScore() != null ? user.getCreditScore() : 100));
    }

    @PutMapping("/phone")
    public Result<?> changePhone(@RequestHeader("Authorization") String authHeader,
                                  @RequestBody Map<String, String> body) {
        Long userId = getUserId(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        String password = body.get("password");
        String newPhone = body.get("phone");

        if (password == null || password.isEmpty()) {
            return Result.error("请输入当前密码");
        }
        if (newPhone == null || newPhone.isEmpty()) {
            return Result.error("请输入新手机号");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return Result.error("密码错误");
        }
        if (userRepository.findByPhone(newPhone).isPresent()) {
            return Result.error("该手机号已被注册");
        }

        user.setPhone(newPhone);
        userRepository.save(user);
        return Result.success("手机号修改成功", null);
    }

    @PutMapping("/deactivate")
    public Result<?> deactivate(@RequestHeader("Authorization") String authHeader,
                                 @RequestBody Map<String, String> body) {
        Long userId = getUserId(authHeader);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        String password = body.get("password");
        if (password == null || password.isEmpty()) {
            return Result.error("请输入密码");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return Result.error("密码错误");
        }

        user.setStatus(-2); // -2 = 已申请注销（冷静期）
        user.setDeactivatedAt(LocalDateTime.now());
        userRepository.save(user);
        return Result.success("账号已申请注销，7天内可联系客服恢复", null);
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
}
