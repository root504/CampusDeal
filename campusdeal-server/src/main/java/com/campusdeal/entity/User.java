package com.campusdeal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@DynamicUpdate
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "student_id", length = 20, unique = true)
    private String studentId;

    @Column(name = "wechat_id", length = 50)
    private String wechatId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 100)
    private String school;

    @Column(length = 100)
    private String college;

    @Column(name = "dorm_area", length = 50)
    private String campus;

    @Column(length = 20)
    private String grade;

    @Column(name = "credit_score")
    private Integer creditScore = 100;

    @Column(name = "is_verified")
    private Integer isVerified = 0;

    private Integer role = 0;

    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    public User() {}

    public User(Long id, String phone, String passwordHash, String studentId, String wechatId,
                String username, String avatarUrl, String school, String college, String campus,
                String grade, Integer creditScore, Integer isVerified, Integer role, Integer status,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.studentId = studentId;
        this.wechatId = wechatId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.school = school;
        this.college = college;
        this.campus = campus;
        this.grade = grade;
        this.creditScore = creditScore;
        this.isVerified = isVerified;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String phone;
        private String passwordHash;
        private String studentId;
        private String wechatId;
        private String username;
        private String avatarUrl;
        private String school;
        private String college;
        private String campus;
        private String grade;
        private Integer creditScore = 100;
        private Integer isVerified = 0;
        private Integer role = 0;
        private Integer status = 1;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        UserBuilder() {}

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder phone(String phone) { this.phone = phone; return this; }
        public UserBuilder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public UserBuilder studentId(String studentId) { this.studentId = studentId; return this; }
        public UserBuilder wechatId(String wechatId) { this.wechatId = wechatId; return this; }
        public UserBuilder username(String username) { this.username = username; return this; }
        public UserBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public UserBuilder school(String school) { this.school = school; return this; }
        public UserBuilder college(String college) { this.college = college; return this; }
        public UserBuilder campus(String campus) { this.campus = campus; return this; }
        public UserBuilder grade(String grade) { this.grade = grade; return this; }
        public UserBuilder creditScore(Integer creditScore) { this.creditScore = creditScore; return this; }
        public UserBuilder isVerified(Integer isVerified) { this.isVerified = isVerified; return this; }
        public UserBuilder role(Integer role) { this.role = role; return this; }
        public UserBuilder status(Integer status) { this.status = status; return this; }
        public UserBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UserBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public User build() {
            User user = new User();
            user.id = this.id;
            user.phone = this.phone;
            user.passwordHash = this.passwordHash;
            user.studentId = this.studentId;
            user.wechatId = this.wechatId;
            user.username = this.username;
            user.avatarUrl = this.avatarUrl;
            user.school = this.school;
            user.college = this.college;
            user.campus = this.campus;
            user.grade = this.grade;
            user.creditScore = this.creditScore;
            user.isVerified = this.isVerified;
            user.role = this.role;
            user.status = this.status;
            user.createdAt = this.createdAt;
            user.updatedAt = this.updatedAt;
            return user;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getWechatId() { return wechatId; }
    public void setWechatId(String wechatId) { this.wechatId = wechatId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public Integer getIsVerified() { return isVerified; }
    public void setIsVerified(Integer isVerified) { this.isVerified = isVerified; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getDeactivatedAt() { return deactivatedAt; }
    public void setDeactivatedAt(LocalDateTime deactivatedAt) { this.deactivatedAt = deactivatedAt; }

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
