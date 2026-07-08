package com.campusdeal.dto.response;

import java.time.LocalDateTime;

public class UserProfileResponse {

    private Long id;
    private String phone;
    private String username;
    private String avatarUrl;
    private String school;
    private String college;
    private String campus;
    private String grade;
    private Integer creditScore;
    private Integer isVerified;
    private Integer status;
    private LocalDateTime createdAt;

    public UserProfileResponse() {}

    public UserProfileResponse(Long id, String phone, String username, String avatarUrl,
                               String school, String college, String campus, String grade,
                               Integer creditScore, Integer isVerified, Integer status,
                               LocalDateTime createdAt) {
        this.id = id; this.phone = phone; this.username = username;
        this.avatarUrl = avatarUrl; this.school = school; this.college = college;
        this.campus = campus; this.grade = grade; this.creditScore = creditScore;
        this.isVerified = isVerified; this.status = status;
        this.createdAt = createdAt;
    }

    public static UserProfileResponseBuilder builder() { return new UserProfileResponseBuilder(); }

    public static class UserProfileResponseBuilder {
        private Long id;
        private String phone;
        private String username;
        private String avatarUrl;
        private String school;
        private String college;
        private String campus;
        private String grade;
        private Integer creditScore;
        private Integer isVerified;
        private Integer status;
        private LocalDateTime createdAt;

        UserProfileResponseBuilder() {}
        public UserProfileResponseBuilder id(Long id) { this.id = id; return this; }
        public UserProfileResponseBuilder phone(String phone) { this.phone = phone; return this; }
        public UserProfileResponseBuilder username(String username) { this.username = username; return this; }
        public UserProfileResponseBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public UserProfileResponseBuilder school(String school) { this.school = school; return this; }
        public UserProfileResponseBuilder college(String college) { this.college = college; return this; }
        public UserProfileResponseBuilder campus(String campus) { this.campus = campus; return this; }
        public UserProfileResponseBuilder grade(String grade) { this.grade = grade; return this; }
        public UserProfileResponseBuilder creditScore(Integer creditScore) { this.creditScore = creditScore; return this; }
        public UserProfileResponseBuilder isVerified(Integer isVerified) { this.isVerified = isVerified; return this; }
        public UserProfileResponseBuilder status(Integer status) { this.status = status; return this; }
        public UserProfileResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public UserProfileResponse build() {
            UserProfileResponse r = new UserProfileResponse();
            r.id = this.id; r.phone = this.phone; r.username = this.username;
            r.avatarUrl = this.avatarUrl; r.school = this.school;
            r.college = this.college; r.campus = this.campus;
            r.grade = this.grade; r.creditScore = this.creditScore;
            r.isVerified = this.isVerified; r.status = this.status;
            r.createdAt = this.createdAt;
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
