package com.campusdeal.controller.admin;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.entity.User;
import com.campusdeal.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final AdminService adminService;
    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }


    @GetMapping
    public Result<PageResult<User>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminService.getUsers(keyword, page, size));
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateUserStatus(id, body.get("status"));
        return Result.success(null);
    }

    @PutMapping("/{id}")
    public Result<?> edit(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.editUser(id,
                body.get("username"),
                body.get("phone"),
                body.get("school"),
                body.get("campus"));
        return Result.success("编辑成功", null);
    }

    @PutMapping("/{id}/role")
    public Result<?> updateRole(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateUserRole(id, body.get("role"));
        return Result.success(null);
    }
}
