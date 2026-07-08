package com.campusdeal.controller;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.dto.request.MessageRequest;
import com.campusdeal.entity.Message;
import com.campusdeal.entity.User;
import com.campusdeal.service.MessageService;
import com.campusdeal.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class MessageController {
    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    public MessageController(MessageService messageService, JwtUtil jwtUtil) {
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
    }


    @GetMapping("/api/v1/conversations")
    public Result<List<Map<String, Object>>> conversations(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(messageService.getConversations(userId));
    }

    @GetMapping("/api/v1/conversations/{conversationId}/messages")
    public Result<PageResult<Message>> messages(@RequestHeader("Authorization") String authHeader,
                                                 @PathVariable String conversationId,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "50") int size) {
        return Result.success(messageService.getConversationMessages(conversationId, page, size));
    }

    @PostMapping("/api/v1/messages")
    public Result<Message> sendMessage(@RequestHeader("Authorization") String authHeader,
                                        @Valid @RequestBody MessageRequest request) {
        Long senderId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        if (senderId.equals(request.getReceiverId())) {
            throw new com.campusdeal.common.BusinessException("不能给自己发送消息");
        }
        return Result.success(messageService.sendMessage(
                senderId,
                request.getReceiverId(),
                request.getProductId(),
                request.getContent(),
                request.getMessageType()
        ));
    }

    @PutMapping("/api/v1/messages/read/{conversationId}")
    public Result<?> markRead(@RequestHeader("Authorization") String authHeader,
                               @PathVariable String conversationId) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        messageService.markConversationAsRead(conversationId, userId);
        return Result.success(null);
    }

    @PutMapping("/api/v1/messages/read-all")
    public Result<?> markAllRead(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        messageService.markAllAsRead(userId);
        return Result.success(null);
    }

    @GetMapping("/api/v1/messages/unread-count")
    public Result<Long> unreadCount(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(messageService.getUnreadCount(userId));
    }

    @GetMapping("/api/v1/messages/admin")
    public Result<PageResult<Message>> adminMessages(@RequestHeader("Authorization") String authHeader,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(messageService.getAdminMessages(userId, page, size));
    }

    @GetMapping("/api/v1/messages/admin/list")
    public Result<PageResult<Message>> adminMessageList(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return Result.success(messageService.getAllUserMessages(page, size));
    }

    @PostMapping("/api/v1/messages/admin/reply")
    public Result<Message> adminReply(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String content = (String) body.get("content");
        String messageType = body.containsKey("messageType") ? (String) body.get("messageType") : "system";
        return Result.success(messageService.sendSystemMessage(userId, content, messageType, null));
    }

    @GetMapping("/api/v1/messages/system-notifications")
    public Result<PageResult<Message>> systemNotifications(@RequestHeader("Authorization") String authHeader,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(messageService.getSystemNotifications(userId, page, size));
    }

    // ===== 新增：管理员系统通知管理接口 =====

    /** 管理员获取所有系统通知会话列表（按用户分组） */
    @GetMapping("/api/v1/admin/messages/conversations")
    public Result<List<Map<String, Object>>> adminConversations() {
        return Result.success(messageService.getAdminSystemConversations());
    }

    /** 管理员获取与某用户的系统通知聊天记录 */
    @GetMapping("/api/v1/admin/messages/chat/{userId}")
    public Result<PageResult<Message>> adminChatWithUser(@PathVariable Long userId,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "50") int size) {
        return Result.success(messageService.getAdminChatWithUser(userId, page, size));
    }

    /** 管理员向指定用户发送系统通知 */
    @PostMapping("/api/v1/admin/messages/send")
    public Result<Message> adminSendNotification(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String content = (String) body.get("content");
        String messageType = body.containsKey("messageType") ? (String) body.get("messageType") : "system";
        Long referenceId = body.containsKey("referenceId") ? Long.valueOf(body.get("referenceId").toString()) : null;
        return Result.success(messageService.sendSystemNotificationToUser(userId, content, messageType, referenceId));
    }

    /** 批量发送系统通知给所有用户（公告） */
    @PostMapping("/api/v1/admin/messages/send-all")
    public Result<Integer> adminSendToAll(@RequestBody Map<String, Object> body) {
        String content = (String) body.get("content");
        String messageType = body.containsKey("messageType") ? (String) body.get("messageType") : "announcement";
        Long referenceId = body.containsKey("referenceId") ? Long.valueOf(body.get("referenceId").toString()) : null;

        List<User> allUsers = messageService.getUsersForNotification();
        int sent = 0;
        for (User u : allUsers) {
            Message msg = messageService.sendSystemNotificationToUser(u.getId(), content, messageType, referenceId);
            if (msg != null) sent++;
        }
        return Result.success(sent);
    }

    /** 导出与某用户的聊天记录 */
    @GetMapping("/api/v1/admin/messages/export/{userId}")
    public Result<String> exportChatRecords(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "text") String format) {
        return Result.success(messageService.exportChatRecords(userId, format));
    }

    /** 用户向管理员发送消息（用户端调用） */
    @PostMapping("/api/v1/messages/admin/send")
    public Result<Message> userSendToAdmin(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody Map<String, String> body) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        String content = body.get("content");
        return Result.success(messageService.sendUserMessageToAdmin(userId, content));
    }

    /** 获取所有用户列表（供通知范围选择） */
    @GetMapping("/api/v1/admin/messages/users")
    public Result<List<User>> usersForNotification() {
        return Result.success(messageService.getUsersForNotification());
    }

    // ===== 需求三：管理员用户视图 — 查看任意用户完整会话 =====

    /** 管理员获取所有有会话记录的用户（含 system + user-user） */
    @GetMapping("/api/v1/admin/messages/all-conversations")
    public Result<List<Map<String, Object>>> adminAllConversations() {
        return Result.success(messageService.getAdminAllConversationUsers());
    }

    /** 管理员查看某个用户的全部会话列表（与其前端 Messages.vue 一致） */
    @GetMapping("/api/v1/admin/messages/user-conversations/{userId}")
    public Result<List<Map<String, Object>>> adminUserConversations(@PathVariable Long userId) {
        return Result.success(messageService.getAdminUserConversations(userId));
    }

    /** 管理员以只读方式查看某个用户的任意会话消息 */
    @GetMapping("/api/v1/admin/messages/user-chat/{userId}/{conversationId}")
    public Result<PageResult<Message>> adminUserChatMessages(@PathVariable Long userId,
                                                              @PathVariable String conversationId,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "50") int size) {
        return Result.success(messageService.getAdminAnyConversationMessages(userId, conversationId, page, size));
    }

    /** 管理员获取所有系统通知（跨用户） */
    @GetMapping("/api/v1/admin/messages/system-notifications")
    public Result<PageResult<Message>> adminSystemNotifications(@RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        return Result.success(messageService.getAllSystemNotifications(page, size));
    }

    /** 管理员获取某用户的聊天对象伙伴列表 */
    @GetMapping("/api/v1/admin/messages/user-chat-partners/{userId}")
    public Result<List<Map<String, Object>>> userChatPartners(@PathVariable Long userId) {
        return Result.success(messageService.getUserChatPartners(userId));
    }
}
