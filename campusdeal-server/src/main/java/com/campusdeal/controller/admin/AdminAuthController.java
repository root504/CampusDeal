package com.campusdeal.controller.admin;

import com.campusdeal.common.BusinessException;
import com.campusdeal.common.Result;
import com.campusdeal.dto.request.AdminLoginRequest;
import com.campusdeal.dto.response.LoginResponse;
import com.campusdeal.entity.Admin;
import com.campusdeal.repository.AdminRepository;
import com.campusdeal.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public AdminAuthController(AdminRepository adminRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        Admin admin;
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            admin = adminRepository.findByPhone(request.getPhone())
                    .orElseThrow(() -> new BusinessException("手机号或密码错误"));
        } else if (request.getUsername() != null && !request.getUsername().isBlank()) {
            admin = adminRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        } else {
            throw new BusinessException("手机号或用户名不能为空");
        }

        if (admin.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException("手机号或密码错误");
        }

        String accessToken = jwtUtil.generateAdminToken(admin.getId(), admin.getUsername());

        return Result.success(LoginResponse.builder()
                .userId(admin.getId())
                .username(admin.getUsername())
                .accessToken(accessToken)
                .build());
    }
}
