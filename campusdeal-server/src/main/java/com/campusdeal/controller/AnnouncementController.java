package com.campusdeal.controller;

import com.campusdeal.common.Result;
import com.campusdeal.entity.Announcement;
import com.campusdeal.repository.AnnouncementRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementController(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    /**
     * 公开接口：获取所有已发布公告（status=1），按创建时间倒序
     * 无需认证，所有用户（含新注册用户）均可访问
     */
    @GetMapping
    public Result<List<Announcement>> listPublished() {
        List<Announcement> list = announcementRepository.findByStatusOrderByCreatedAtDesc(1);
        return Result.success(list);
    }
}
