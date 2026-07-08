package com.campusdeal.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AdminLoginRequest {

    private String phone;

    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    public AdminLoginRequest() {}

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
