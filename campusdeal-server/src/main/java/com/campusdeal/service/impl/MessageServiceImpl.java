package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.common.PageResult;
import com.campusdeal.entity.Message;
import com.campusdeal.entity.User;
import com.campusdeal.repository.MessageRepository;
import com.campusdeal.repository.UserRepository;
import com.campusdeal.service.MessageService;
import com.campusdeal.service.SensitiveWordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SensitiveWordService sensitiveWordService;
    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository,
                              SensitiveWordService sensitiveWordService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.sensitiveWordService = sensitiveWordService;
    }


    @Override
    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, Long productId, String content, String messageType) {
        if (!userRepository.existsById(receiverId)) {
            throw new BusinessException("接收者不存在");
        }

        // 敏感词检测
        sensitiveWordService.checkContent(content);

        String conversationId = buildConversationId(senderId, receiverId, productId);

        Message message = Message.builder()
                .conversationId(conversationId)
                .fromUserId(senderId)
                .toUserId(receiverId)
                .productId(productId)
                .content(content)
                .messageType(messageType != null ? messageType : "text")
                .isRead(0)
                .build();

        return messageRepository.save(message);
    }

    @Override
    public PageResult<Message> getConversationMessages(String conversationId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        String canonical = canonicalConversationId(conversationId);
        Page<Message> msgPage = messageRepository.findMessagesByUserPair(canonical, pageable);
        return PageResult.of(msgPage.getContent(), msgPage.getTotalElements(), page, size);
    }

    @Override
    public List<Map<String, Object>> getConversations(Long userId) {
        List<Object[]> results = messageRepository.findConversationsByUserId(userId);
        List<Map<String, Object>> rawConversations = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> conv = new HashMap<>();
            String rawConvId = String.valueOf(row[0]);
            String canonicalId = canonicalConversationId(rawConvId);
            conv.put("conversationId", canonicalId);
            conv.put("rawConversationId", rawConvId);
            conv.put("unreadCount", row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L);
            conv.put("lastMessage", row[2] != null ? String.valueOf(row[2]) : "");
            conv.put("lastTime", row[3]);
            conv.put("otherUserId", row[4] instanceof Number ? ((Number) row[4]).longValue() : null);
            conv.put("productId", row[5] instanceof Number ? ((Number) row[5]).longValue() : null);
            conv.put("otherUserName", row.length > 7 && row[7] != null ? String.valueOf(row[7]) : null);
            conv.put("lastMessageFromUserId", row.length > 6 && row[6] instanceof Number ? ((Number) row[6]).longValue() : 0L);
            rawConversations.add(conv);
        }

        // 按 otherUserId 去重合并：同一对用户只保留一个会话
        Map<Long, Map<String, Object>> merged = new LinkedHashMap<>();
        for (Map<String, Object> conv : rawConversations) {
            Long otherUid = (Long) conv.get("otherUserId");
            if (otherUid == null) {
                // 无法识别的会话直接保留
                merged.put((long) merged.size() * -1, conv);
                continue;
            }
            Map<String, Object> existing = merged.get(otherUid);
            if (existing == null) {
                merged.put(otherUid, conv);
            } else {
                // 合并：累加未读，保留最新的 lastTime/lastMessage/productId
                long existingUnread = (Long) existing.get("unreadCount");
                long newUnread = (Long) conv.get("unreadCount");
                existing.put("unreadCount", existingUnread + newUnread);

                Object existingTime = existing.get("lastTime");
                Object newTime = conv.get("lastTime");
                if (existingTime == null || (newTime != null && compareTimestamps(newTime, existingTime) > 0)) {
                    existing.put("lastTime", newTime);
                    existing.put("lastMessage", conv.get("lastMessage"));
                    existing.put("productId", conv.get("productId"));
                    existing.put("rawConversationId", conv.get("rawConversationId"));
                    existing.put("lastMessageFromUserId", conv.get("lastMessageFromUserId"));
                }
            }
        }

        List<Map<String, Object>> conversations = new ArrayList<>(merged.values());

        // 按 lastTime 降序重排
        conversations.sort((a, b) -> {
            Object ta = a.get("lastTime");
            Object tb = b.get("lastTime");
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return -compareTimestamps(ta, tb);
        });

        return conversations;
    }

    /** Compare two timestamp objects (java.sql.Timestamp or LocalDateTime) */
    private int compareTimestamps(Object a, Object b) {
        if (a instanceof java.sql.Timestamp && b instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) a).compareTo((java.sql.Timestamp) b);
        }
        if (a instanceof LocalDateTime && b instanceof LocalDateTime) {
            return ((LocalDateTime) a).compareTo((LocalDateTime) b);
        }
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    /**
     * 从任意格式的 conversationId 提取规范形式 "smaller_larger"。
     * "1_2_null" → "1_2", "1_2_5" → "1_2", "1_2" → "1_2"
     */
    private String canonicalConversationId(String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) return conversationId;
        String[] parts = conversationId.split("_");
        if (parts.length >= 2) {
            try {
                long a = Long.parseLong(parts[0]);
                long b = Long.parseLong(parts[1]);
                long smaller = Math.min(a, b);
                long larger = Math.max(a, b);
                return smaller + "_" + larger;
            } catch (NumberFormatException e) {
                return conversationId;
            }
        }
        return conversationId;
    }

    @Override
    public PageResult<Message> getAdminMessages(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Message> msgPage = messageRepository.findByConversationIdOrderByCreatedAtAsc("system_" + userId, pageable);
        return PageResult.of(msgPage.getContent(), msgPage.getTotalElements(), page, size);
    }

    @Override
    public PageResult<Message> getSystemNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Message> msgPage = messageRepository.findSystemNotificationsByUserId(userId, pageable);
        return PageResult.of(msgPage.getContent(), msgPage.getTotalElements(), page, size);
    }

    @Override
    public PageResult<Message> getAllUserMessages(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Message> msgPage = messageRepository.findAllUserMessages(pageable);
        return PageResult.of(msgPage.getContent(), msgPage.getTotalElements(), page, size);
    }

    @Override
    @Transactional
    public Message sendSystemMessage(Long receiverId, String content, String messageType, Long referenceId) {
        if (!userRepository.existsById(receiverId)) {
            throw new BusinessException("接收者不存在");
        }

        // 敏感词检测
        sensitiveWordService.checkContent(content);

        // 公告去重：检查是否已向该用户发送过同一条公告
        if ("announcement".equals(messageType) && referenceId != null) {
            if (messageRepository.existsAnnouncementForUser(receiverId, referenceId)) {
                return null; // 已发送过，跳过
            }
        }

        User systemUser = getSystemUser(receiverId);

        Message message = Message.builder()
                .conversationId("system_" + receiverId)
                .fromUserId(systemUser.getId())
                .toUserId(receiverId)
                .content(content)
                .messageType(messageType != null ? messageType : "system")
                .referenceId(referenceId)
                .isRead(0)
                .build();
        return messageRepository.save(message);
    }

    /**
     * 获取系统用户（最小的非排除用户），用作系统消息发送者或管理员接收者。
     * 当系统中只有一个用户时，返回该用户自身。
     */
    private User getSystemUser(Long excludeUserId) {
        List<User> candidates = userRepository
                .findAll(PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "id")))
                .getContent();
        User user = candidates.stream()
                .filter(u -> !u.getId().equals(excludeUserId))
                .findFirst()
                .orElse(candidates.isEmpty() ? null : candidates.get(0));
        if (user == null) {
            throw new BusinessException("系统中无可用用户");
        }
        return user;
    }

    @Override
    @Transactional
    public void markConversationAsRead(String conversationId, Long userId) {
        String canonical = canonicalConversationId(conversationId);
        messageRepository.markUserPairAsRead(canonical, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        messageRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    @Override
    public List<Map<String, Object>> getAdminSystemConversations() {
        List<Long> userIds = messageRepository.findSystemConversationUserIds();
        List<Map<String, Object>> conversations = new ArrayList<>();

        for (Long uid : userIds) {
            User user = userRepository.findById(uid).orElse(null);
            if (user == null) continue;

            String convId = "system_" + uid;
            java.util.Optional<Message> latestOpt = messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(convId);
            long unread = messageRepository.countUnreadFromUser(uid);

            Map<String, Object> conv = new HashMap<>();
            conv.put("userId", uid);
            conv.put("userName", user.getUsername());
            conv.put("lastMessage", latestOpt.map(Message::getContent).orElse(""));
            conv.put("lastTime", latestOpt.map(Message::getCreatedAt).orElse(user.getCreatedAt()));
            conv.put("unreadCount", unread);
            conversations.add(conv);
        }

        // Sort by lastTime DESC
        conversations.sort((a, b) -> {
            Object ta = a.get("lastTime");
            Object tb = b.get("lastTime");
            if (ta instanceof LocalDateTime && tb instanceof LocalDateTime) {
                return ((LocalDateTime) tb).compareTo((LocalDateTime) ta);
            }
            return 0;
        });

        return conversations;
    }

    @Override
    public PageResult<Message> getAdminChatWithUser(Long userId, int page, int size) {
        String conversationId = "system_" + userId;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Message> msgPage = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);
        return PageResult.of(msgPage.getContent(), msgPage.getTotalElements(), page, size);
    }

    @Override
    @Transactional
    public Message sendSystemNotificationToUser(Long userId, String content, String messageType, Long referenceId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("接收者不存在");
        }
        return sendSystemMessage(userId, content, messageType, referenceId);
    }

    @Override
    public String exportChatRecords(Long userId, String format) {
        String conversationId = "system_" + userId;
        Pageable all = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Message> msgPage = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, all);

        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getUsername() : ("用户" + userId);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if ("json".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append("[\n");
            List<Message> msgs = msgPage.getContent();
            for (int i = 0; i < msgs.size(); i++) {
                Message m = msgs.get(i);
                sb.append("  {\n");
                sb.append("    \"id\": ").append(m.getId()).append(",\n");
                sb.append("    \"direction\": \"").append(m.getFromUserId() == null ? "admin→user" : "user→admin").append("\",\n");
                sb.append("    \"content\": \"").append(escapeJson(m.getContent())).append("\",\n");
                sb.append("    \"time\": \"").append(m.getCreatedAt() != null ? m.getCreatedAt().format(dtf) : "").append("\"\n");
                sb.append("  }").append(i < msgs.size() - 1 ? "," : "").append("\n");
            }
            sb.append("]\n");
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 聊天记录导出 ===\n");
            sb.append("用户: ").append(userName).append(" (ID:").append(userId).append(")\n");
            sb.append("导出时间: ").append(LocalDateTime.now().format(dtf)).append("\n");
            sb.append("================================\n\n");

            for (Message m : msgPage.getContent()) {
                String direction = m.getFromUserId() == null ? "管理员" : userName;
                String time = m.getCreatedAt() != null ? m.getCreatedAt().format(dtf) : "";
                sb.append("[").append(time).append("] ").append(direction).append(":\n");
                sb.append(m.getContent()).append("\n\n");
            }
            return sb.toString();
        }
    }

    @Override
    public List<User> getUsersForNotification() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Override
    @Transactional
    public Message sendUserMessageToAdmin(Long userId, String content) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        User adminUser = getSystemUser(userId);
        String conversationId = "system_" + userId;
        Message message = Message.builder()
                .conversationId(conversationId)
                .fromUserId(userId)
                .toUserId(adminUser.getId())
                .content(content)
                .messageType("system")
                .isRead(0)
                .build();
        return messageRepository.save(message);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public List<Map<String, Object>> getAdminUserConversations(Long userId) {
        return getConversations(userId);
    }

    @Override
    public PageResult<Message> getAdminAnyConversationMessages(Long userId, String conversationId, int page, int size) {
        // 验证 conversationId 属于该用户（防止越权）
        if (!conversationId.contains("_" + userId) && !conversationId.startsWith(userId + "_")) {
            throw new BusinessException("无权查看该会话");
        }
        return getConversationMessages(conversationId, page, size);
    }

    @Override
    public List<Map<String, Object>> getAdminAllConversationUsers() {
        // 返回所有有任意消息记录的用户（包括 system + user-user）
        // 复用已有逻辑：获取所有用户，合并 system 会话和普通会话的用户
        Map<Long, Map<String, Object>> userMap = new HashMap<>();

        // 1. 系统会话用户
        List<Long> systemUserIds = messageRepository.findSystemConversationUserIds();
        for (Long uid : systemUserIds) {
            User user = userRepository.findById(uid).orElse(null);
            if (user == null) continue;
            String convId = "system_" + uid;
            Pageable top1 = PageRequest.of(0, 1);
            List<Message> latestMsgs = messageRepository.findLatestBySystemConversation(convId, top1);
            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", uid);
            entry.put("userName", user.getUsername());
            entry.put("lastMessage", latestMsgs.isEmpty() ? "" : latestMsgs.get(0).getContent());
            entry.put("lastTime", latestMsgs.isEmpty() ? user.getCreatedAt() : latestMsgs.get(0).getCreatedAt());
            entry.put("unreadCount", messageRepository.countUnreadFromUser(uid));
            userMap.put(uid, entry);
        }

        // 2. 普通会话用户（user-user chats）
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (userMap.containsKey(user.getId())) continue;
            List<Object[]> convs = messageRepository.findConversationsByUserId(user.getId());
            if (convs.isEmpty()) continue;
            // 取最近一条会话
            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", user.getId());
            entry.put("userName", user.getUsername());
            Object[] latestConv = convs.get(0);
            entry.put("lastMessage", latestConv[2] != null ? String.valueOf(latestConv[2]) : "");
            entry.put("lastTime", latestConv[3]);
            entry.put("unreadCount", 0L);
            userMap.put(user.getId(), entry);
        }

        List<Map<String, Object>> result = new ArrayList<>(userMap.values());
        result.sort((a, b) -> {
            Object ta = a.get("lastTime");
            Object tb = b.get("lastTime");
            if (ta instanceof LocalDateTime && tb instanceof LocalDateTime) {
                return ((LocalDateTime) tb).compareTo((LocalDateTime) ta);
            }
            return 0;
        });
        return result;
    }

    @Override
    public PageResult<Message> getAllSystemNotifications(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Message> pageResult = messageRepository.findAllSystemNotifications(pageable);
        return PageResult.of(pageResult.getContent(), pageResult.getTotalElements(), page, size);
    }

    private String buildConversationId(Long userA, Long userB, Long productId) {
        long smaller = Math.min(userA, userB);
        long larger = Math.max(userA, userB);
        return smaller + "_" + larger;
    }

    @Override
    public List<Map<String, Object>> getUserChatPartners(Long userId) {
        List<Long> partnerIds = messageRepository.findChatPartnersByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long pid : partnerIds) {
            User user = userRepository.findById(pid).orElse(null);
            Map<String, Object> entry = new HashMap<>();
            entry.put("userId", pid);
            entry.put("userName", user != null ? user.getUsername() : ("用户" + pid));
            result.add(entry);
        }
        return result;
    }
}
