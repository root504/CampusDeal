package com.campusdeal.service;

import com.campusdeal.common.PageResult;
import com.campusdeal.entity.Message;
import com.campusdeal.entity.User;

import java.util.List;
import java.util.Map;

public interface MessageService {
    Message sendMessage(Long senderId, Long receiverId, Long productId, String content, String messageType);
    Message sendSystemMessage(Long receiverId, String content, String messageType, Long referenceId);
    PageResult<Message> getConversationMessages(String conversationId, int page, int size);
    List<Map<String, Object>> getConversations(Long userId);
    PageResult<Message> getAdminMessages(Long userId, int page, int size);
    PageResult<Message> getSystemNotifications(Long userId, int page, int size);
    PageResult<Message> getAllUserMessages(int page, int size);
    void markConversationAsRead(String conversationId, Long userId);
    void markAllAsRead(Long userId);
    long getUnreadCount(Long userId);

    // Admin system conversation management
    List<Map<String, Object>> getAdminSystemConversations();
    PageResult<Message> getAdminChatWithUser(Long userId, int page, int size);
    Message sendSystemNotificationToUser(Long userId, String content, String messageType, Long referenceId);
    String exportChatRecords(Long userId, String format);
    List<User> getUsersForNotification();
    Message sendUserMessageToAdmin(Long userId, String content);

    // Admin user conversation browsing (用户视图 — 查看任意用户的完整会话)
    List<Map<String, Object>> getAdminUserConversations(Long userId);
    PageResult<Message> getAdminAnyConversationMessages(Long userId, String conversationId, int page, int size);
    List<Map<String, Object>> getAdminAllConversationUsers();

    // Admin: get all system notifications
    PageResult<Message> getAllSystemNotifications(int page, int size);

    // Admin: get chat partners for a user
    List<Map<String, Object>> getUserChatPartners(Long userId);
}
