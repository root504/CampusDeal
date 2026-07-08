package com.campusdeal.dto.response;

public class LoginResponse {

    private Long userId;
    private String phone;
    private String username;
    private String avatarUrl;
    private String school;
    private String college;
    private String campus;
    private String grade;
    private Integer creditScore;
    private Integer role;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;

    public LoginResponse() {}

    public LoginResponse(Long userId, String phone, String username, String avatarUrl,
                         String school, String college, String campus, String grade,
                         Integer creditScore, Integer role, String accessToken, String refreshToken, long expiresIn) {
        this.userId = userId;
        this.phone = phone;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.school = school;
        this.college = college;
        this.campus = campus;
        this.grade = grade;
        this.creditScore = creditScore;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public static LoginResponseBuilder builder() { return new LoginResponseBuilder(); }

    public static class LoginResponseBuilder {
        private Long userId;
        private String phone;
        private String username;
        private String avatarUrl;
        private String school;
        private String college;
        private String campus;
        private String grade;
        private Integer creditScore;
        private Integer role;
        private String accessToken;
        private String refreshToken;
        private long expiresIn;

        LoginResponseBuilder() {}
        public LoginResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public LoginResponseBuilder phone(String phone) { this.phone = phone; return this; }
        public LoginResponseBuilder username(String username) { this.username = username; return this; }
        public LoginResponseBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public LoginResponseBuilder school(String school) { this.school = school; return this; }
        public LoginResponseBuilder college(String college) { this.college = college; return this; }
        public LoginResponseBuilder campus(String campus) { this.campus = campus; return this; }
        public LoginResponseBuilder grade(String grade) { this.grade = grade; return this; }
        public LoginResponseBuilder creditScore(Integer creditScore) { this.creditScore = creditScore; return this; }
        public LoginResponseBuilder role(Integer role) { this.role = role; return this; }
        public LoginResponseBuilder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public LoginResponseBuilder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public LoginResponseBuilder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }
        public LoginResponse build() {
            LoginResponse r = new LoginResponse();
            r.userId = this.userId; r.phone = this.phone;
            r.username = this.username; r.avatarUrl = this.avatarUrl;
            r.school = this.school; r.college = this.college;
            r.campus = this.campus; r.grade = this.grade;
            r.creditScore = this.creditScore;
            r.role = this.role;
            r.accessToken = this.accessToken; r.refreshToken = this.refreshToken;
            r.expiresIn = this.expiresIn;
            return r;
        }
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
