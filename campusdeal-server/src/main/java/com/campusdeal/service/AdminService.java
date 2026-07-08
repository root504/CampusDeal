package com.campusdeal.service;

import com.campusdeal.common.PageResult;
import com.campusdeal.entity.*;

import java.util.Map;

public interface AdminService {
    Map<String, Object> dashboard();
    Object getTrend();
    Object getCategoryDistribution();
    PageResult<User> getUsers(String keyword, int page, int size);
    void updateUserStatus(Long userId, Integer status);
    void editUser(Long userId, String nickname, String phone, String school, String campus);
    void updateUserRole(Long userId, Integer role);
    PageResult<Announcement> getAnnouncements(int page, int size);
    Announcement createAnnouncement(Long adminId, String title, String content);
    Announcement updateAnnouncement(Long id, String title, String content);
    void deleteAnnouncement(Long id);
    Map<String, Object> syncAnnouncementMessages();
}
