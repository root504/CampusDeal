package com.campusdeal.controller;

import com.campusdeal.common.Result;
import com.campusdeal.dto.request.LoginRequest;
import com.campusdeal.dto.request.RegisterRequest;
import com.campusdeal.dto.response.LoginResponse;
import com.campusdeal.dto.response.UserProfileResponse;
import com.campusdeal.service.AuthService;
import com.campusdeal.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return Result.success(authService.refreshToken(refreshToken));
    }

    @GetMapping("/me")
    public Result<UserProfileResponse> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(authService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<UserProfileResponse> updateProfile(@RequestHeader("Authorization") String authHeader,
                                                      @RequestBody RegisterRequest request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        return Result.success(authService.updateProfile(userId, request));
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return Result.success(null);
    }
}
