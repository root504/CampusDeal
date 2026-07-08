package com.campusdeal.dto.request;

public class UpdateProfileRequest {

    private String username;
    private String avatarUrl;
    private String school;
    private String college;
    private String campus;
    private String grade;

    public UpdateProfileRequest() {}

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
}
