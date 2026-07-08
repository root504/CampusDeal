package com.campusdeal.controller.admin;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.entity.Announcement;
import com.campusdeal.service.AdminService;
import com.campusdeal.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/announcements")
public class AdminAnnouncementController {
    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    public AdminAnnouncementController(AdminService adminService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }


    @GetMapping
    public Result<PageResult<Announcement>> list(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminService.getAnnouncements(page, size));
    }

    @PostMapping
    public Result<Announcement> create(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody Map<String, String> body) {
        Long adminId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(adminService.createAnnouncement(adminId, body.get("title"), body.get("content")));
    }

    @PutMapping("/{id}")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return Result.success(adminService.updateAnnouncement(id, body.get("title"), body.get("content")));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        adminService.deleteAnnouncement(id);
        return Result.success(null);
    }

    /** 同步公告消息：将已发布公告补推为系统通知消息（已存在的自动跳过） */
    @PostMapping("/sync-messages")
    public Result<Map<String, Object>> syncMessages() {
        return Result.success(adminService.syncAnnouncementMessages());
    }
}
