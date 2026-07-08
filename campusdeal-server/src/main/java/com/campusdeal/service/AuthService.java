package com.campusdeal.service;

import com.campusdeal.dto.request.LoginRequest;
import com.campusdeal.dto.request.RegisterRequest;
import com.campusdeal.dto.response.LoginResponse;
import com.campusdeal.dto.response.UserProfileResponse;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    UserProfileResponse getProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, RegisterRequest request);
    void logout(String accessToken);
}
