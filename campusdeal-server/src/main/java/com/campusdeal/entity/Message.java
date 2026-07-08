package com.campusdeal.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", length = 50)
    private String conversationId;

    @Column(name = "from_user_id", nullable = true)
    private Long fromUserId;

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "message_type", length = 20)
    private String messageType = "text";

    @Column(name = "is_read")
    private Integer isRead = 0;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Transient fields
    @Transient
    private String fromUserName;

    @Transient
    private String fromUserAvatar;

    public Message() {}

    public Message(Long id, String conversationId, Long fromUserId, Long toUserId, Long productId,
                   String content, String messageType, Integer isRead, LocalDateTime createdAt,
                   String fromUserName, String fromUserAvatar) {
        this.id = id; this.conversationId = conversationId; this.fromUserId = fromUserId;
        this.toUserId = toUserId; this.productId = productId; this.content = content;
        this.messageType = messageType; this.isRead = isRead; this.createdAt = createdAt;
        this.fromUserName = fromUserName; this.fromUserAvatar = fromUserAvatar;
    }

    public static MessageBuilder builder() { return new MessageBuilder(); }

    public static class MessageBuilder {
        private Long id;
        private String conversationId;
        private Long fromUserId;
        private Long toUserId;
        private Long productId;
        private String content;
        private String messageType = "text";
        private Integer isRead = 0;
        private Long referenceId;
        private LocalDateTime createdAt;
        private String fromUserName;
        private String fromUserAvatar;
        MessageBuilder() {}
        public MessageBuilder id(Long id) { this.id = id; return this; }
        public MessageBuilder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
        public MessageBuilder fromUserId(Long fromUserId) { this.fromUserId = fromUserId; return this; }
        public MessageBuilder senderId(Long senderId) { this.fromUserId = senderId; return this; }
        public MessageBuilder toUserId(Long toUserId) { this.toUserId = toUserId; return this; }
        public MessageBuilder receiverId(Long receiverId) { this.toUserId = receiverId; return this; }
        public MessageBuilder productId(Long productId) { this.productId = productId; return this; }
        public MessageBuilder content(String content) { this.content = content; return this; }
        public MessageBuilder messageType(String messageType) { this.messageType = messageType; return this; }
        public MessageBuilder isRead(Integer isRead) { this.isRead = isRead; return this; }
        public MessageBuilder referenceId(Long referenceId) { this.referenceId = referenceId; return this; }
        public MessageBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public MessageBuilder fromUserName(String fromUserName) { this.fromUserName = fromUserName; return this; }
        public MessageBuilder fromUserAvatar(String fromUserAvatar) { this.fromUserAvatar = fromUserAvatar; return this; }
        public Message build() {
            Message m = new Message();
            m.id = this.id; m.conversationId = this.conversationId;
            m.fromUserId = this.fromUserId; m.toUserId = this.toUserId;
            m.productId = this.productId; m.content = this.content;
            m.messageType = this.messageType; m.isRead = this.isRead;
            m.referenceId = this.referenceId; m.createdAt = this.createdAt;
            m.fromUserName = this.fromUserName; m.fromUserAvatar = this.fromUserAvatar;
            return m;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getFromUserName() { return fromUserName; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }
    public String getFromUserAvatar() { return fromUserAvatar; }
    public void setFromUserAvatar(String fromUserAvatar) { this.fromUserAvatar = fromUserAvatar; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
