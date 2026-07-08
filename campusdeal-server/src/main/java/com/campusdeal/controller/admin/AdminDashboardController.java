package com.campusdeal.controller.admin;

import com.campusdeal.common.Result;
import com.campusdeal.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {
    private final AdminService adminService;
    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }


    @GetMapping
    public Result<Map<String, Object>> dashboard() {
        return Result.success(adminService.dashboard());
    }

    @GetMapping("/trend")
    public Result<Object> trend() {
        return Result.success(adminService.getTrend());
    }

    @GetMapping("/category-dist")
    public Result<Object> categoryDistribution() {
        return Result.success(adminService.getCategoryDistribution());
    }
}
