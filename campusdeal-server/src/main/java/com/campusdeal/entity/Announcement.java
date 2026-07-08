package com.campusdeal.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Announcement() {}

    public Announcement(Long id, Long adminId, String title, String content, Integer priority,
                        Integer status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.adminId = adminId; this.title = title; this.content = content;
        this.priority = priority; this.status = status;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public static AnnouncementBuilder builder() { return new AnnouncementBuilder(); }

    public static class AnnouncementBuilder {
        private Long id;
        private Long adminId;
        private String title;
        private String content;
        private Integer priority = 0;
        private Integer status = 1;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        AnnouncementBuilder() {}
        public AnnouncementBuilder id(Long id) { this.id = id; return this; }
        public AnnouncementBuilder adminId(Long adminId) { this.adminId = adminId; return this; }
        public AnnouncementBuilder title(String title) { this.title = title; return this; }
        public AnnouncementBuilder content(String content) { this.content = content; return this; }
        public AnnouncementBuilder priority(Integer priority) { this.priority = priority; return this; }
        public AnnouncementBuilder status(Integer status) { this.status = status; return this; }
        public AnnouncementBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AnnouncementBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Announcement build() {
            Announcement a = new Announcement();
            a.id = this.id; a.adminId = this.adminId; a.title = this.title; a.content = this.content;
            a.priority = this.priority; a.status = this.status;
            a.createdAt = this.createdAt; a.updatedAt = this.updatedAt;
            return a;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
