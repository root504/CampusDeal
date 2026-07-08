package com.campusdeal.repository;

import com.campusdeal.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversationId IN :conversationIds AND m.createdAt = " +
           "(SELECT MAX(m2.createdAt) FROM Message m2 WHERE m2.conversationId = m.conversationId) " +
           "ORDER BY m.createdAt DESC")
    List<Message> findLatestMessagesByConversationIds(@Param("conversationIds") List<String> conversationIds);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.toUserId = :userId AND m.isRead = 0")
    long countUnreadByReceiverId(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId AND m.toUserId = :userId AND m.isRead = 0")
    long countUnreadByConversationId(@Param("conversationId") String conversationId, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = 1 WHERE m.conversationId = :conversationId AND m.toUserId = :userId AND m.isRead = 0")
    void markConversationAsRead(@Param("conversationId") String conversationId, @Param("userId") Long userId);

    @Query("SELECT DISTINCT m.conversationId FROM Message m WHERE m.fromUserId = :userId OR m.toUserId = :userId")
    List<String> findConversationIdsByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT t.*, u.username as other_user_name FROM (" +
           "SELECT m.conversation_id, " +
           "SUM(CASE WHEN m.to_user_id = :userId AND m.is_read = 0 THEN 1 ELSE 0 END) as unread_count, " +
           "(SELECT m2.content FROM messages m2 WHERE m2.conversation_id = m.conversation_id ORDER BY m2.created_at DESC LIMIT 1) as last_message, " +
           "(SELECT m2.created_at FROM messages m2 WHERE m2.conversation_id = m.conversation_id ORDER BY m2.created_at DESC LIMIT 1) as last_time, " +
           "COALESCE(" +
           "  (SELECT m3.from_user_id FROM messages m3 WHERE m3.conversation_id = m.conversation_id AND m3.from_user_id != :userId LIMIT 1), " +
           "  (SELECT m3.to_user_id FROM messages m3 WHERE m3.conversation_id = m.conversation_id AND m3.to_user_id != :userId LIMIT 1) " +
           ") as other_user_id, " +
           "(SELECT m2.product_id FROM messages m2 WHERE m2.conversation_id = m.conversation_id ORDER BY m2.created_at DESC LIMIT 1) as product_id, " +
           "(SELECT m2.from_user_id FROM messages m2 WHERE m2.conversation_id = m.conversation_id ORDER BY m2.created_at DESC LIMIT 1) as last_message_from_user_id " +
           "FROM messages m WHERE (m.from_user_id = :userId OR m.to_user_id = :userId) AND m.conversation_id NOT LIKE 'system\\_%' " +
           "GROUP BY m.conversation_id) t " +
           "LEFT JOIN users u ON u.id = t.other_user_id ORDER BY t.last_time DESC", nativeQuery = true)
    List<Object[]> findConversationsByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.fromUserId IS NULL AND m.toUserId = :userId ORDER BY m.createdAt DESC")
    Page<Message> findAdminMessagesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.toUserId = :userId AND m.messageType IN ('system', 'announcement', 'audit', 'order') ORDER BY m.createdAt DESC")
    Page<Message> findSystemNotificationsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.toUserId = :userId AND m.isRead = 0")
    long countUnreadMessages(@Param("userId") Long userId);

    // Admin: list all messages from real users (fromUserId > 0)
    @Query("SELECT m FROM Message m WHERE m.fromUserId > 0 ORDER BY m.createdAt DESC")
    Page<Message> findAllUserMessages(Pageable pageable);

    // Check if an announcement message already exists for a user
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Message m WHERE m.toUserId = :userId AND m.messageType = 'announcement' AND m.referenceId = :referenceId")
    boolean existsAnnouncementForUser(@Param("userId") Long userId, @Param("referenceId") Long referenceId);

    // Get all distinct user IDs that have system conversations (conversation_id LIKE 'system_%')
    @Query("SELECT DISTINCT m.toUserId FROM Message m WHERE m.conversationId LIKE 'system\\_%'")
    List<Long> findSystemConversationUserIds();

    // Get latest message for each system conversation user (for admin view conversation list)
    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.createdAt DESC")
    List<Message> findLatestBySystemConversation(@Param("conversationId") String conversationId, Pageable pageable);

    // Count unread system messages from a specific user (messages the user sent in system_ conversation)
    @Query("SELECT COUNT(m) FROM Message m WHERE m.fromUserId = :userId AND m.conversationId LIKE 'system\\_%' AND m.isRead = 0")
    long countUnreadFromUser(@Param("userId") Long userId);

    // Find all messages for a user pair, matching canonicalId exactly OR canonicalId_* pattern
    @Query(value = "SELECT * FROM messages m WHERE m.conversation_id = :canonicalId " +
           "OR m.conversation_id LIKE CONCAT(:canonicalId, '\\_%') " +
           "ORDER BY m.created_at ASC",
           countQuery = "SELECT COUNT(*) FROM messages m WHERE m.conversation_id = :canonicalId " +
           "OR m.conversation_id LIKE CONCAT(:canonicalId, '\\_%')",
           nativeQuery = true)
    Page<Message> findMessagesByUserPair(@Param("canonicalId") String canonicalId, Pageable pageable);

    // Mark all messages for a user pair as read (match canonicalId or canonicalId_*)
    @Modifying
    @Query(value = "UPDATE messages m SET m.is_read = 1 WHERE m.to_user_id = :userId AND m.is_read = 0 " +
           "AND (m.conversation_id = :canonicalId OR m.conversation_id LIKE CONCAT(:canonicalId, '\\_%'))",
           nativeQuery = true)
    void markUserPairAsRead(@Param("canonicalId") String canonicalId, @Param("userId") Long userId);

    // Mark all unread messages as read for a user (一键已读)
    @Modifying
    @Query("UPDATE Message m SET m.isRead = 1 WHERE m.toUserId = :userId AND m.isRead = 0")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    // Admin: get all system notifications (across all users)
    @Query("SELECT m FROM Message m WHERE m.messageType IN ('system', 'announcement', 'audit', 'order') AND m.fromUserId IS NULL ORDER BY m.createdAt DESC")
    Page<Message> findAllSystemNotifications(Pageable pageable);

    // Admin: get distinct chat partners for a user (non-system messages only)
    @Query("SELECT DISTINCT CASE WHEN m.fromUserId = :userId THEN m.toUserId ELSE m.fromUserId END " +
           "FROM Message m WHERE (m.fromUserId = :userId OR m.toUserId = :userId) " +
           "AND m.messageType NOT IN ('system', 'announcement', 'audit', 'order') " +
           "AND m.fromUserId IS NOT NULL")
    List<Long> findChatPartnersByUserId(@Param("userId") Long userId);

    // Get single latest message for a conversation (derived query, more reliable than @Query + Pageable combination)
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(String conversationId);
}
