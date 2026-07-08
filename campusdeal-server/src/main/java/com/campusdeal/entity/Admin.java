package com.campusdeal.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 50)
    private String name;

    @Column(length = 20)
    private String role = "admin";

    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Admin() {}

    public Admin(Long id, String username, String phone, String passwordHash, String name, String role,
                 Integer status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.username = username; this.phone = phone; this.passwordHash = passwordHash;
        this.name = name; this.role = role; this.status = status;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public static AdminBuilder builder() { return new AdminBuilder(); }

    public static class AdminBuilder {
        private Long id;
        private String username;
        private String phone;
        private String passwordHash;
        private String name;
        private String role = "admin";
        private Integer status = 1;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        AdminBuilder() {}
        public AdminBuilder id(Long id) { this.id = id; return this; }
        public AdminBuilder username(String username) { this.username = username; return this; }
        public AdminBuilder phone(String phone) { this.phone = phone; return this; }
        public AdminBuilder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public AdminBuilder name(String name) { this.name = name; return this; }
        public AdminBuilder role(String role) { this.role = role; return this; }
        public AdminBuilder status(Integer status) { this.status = status; return this; }
        public AdminBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AdminBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Admin build() {
            Admin a = new Admin();
            a.id = this.id; a.username = this.username; a.phone = this.phone; a.passwordHash = this.passwordHash;
            a.name = this.name; a.role = this.role; a.status = this.status;
            a.createdAt = this.createdAt; a.updatedAt = this.updatedAt;
            return a;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNickname() { return name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
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
