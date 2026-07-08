package com.campusdeal.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "receiver_name", nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 50)
    private String province;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 50)
    private String district;

    @Column(nullable = false, length = 200)
    private String detail;

    @Column(name = "is_default")
    private Integer isDefault = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Address() {}

    public Address(Long id, Long userId, String receiverName, String phone,
                   String province, String city, String district, String detail,
                   Integer isDefault, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.receiverName = receiverName;
        this.phone = phone;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detail = detail;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AddressBuilder builder() {
        return new AddressBuilder();
    }

    public static class AddressBuilder {
        private Long id;
        private Long userId;
        private String receiverName;
        private String phone;
        private String province;
        private String city;
        private String district;
        private String detail;
        private Integer isDefault = 0;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        AddressBuilder() {}

        public AddressBuilder id(Long id) { this.id = id; return this; }
        public AddressBuilder userId(Long userId) { this.userId = userId; return this; }
        public AddressBuilder receiverName(String receiverName) { this.receiverName = receiverName; return this; }
        public AddressBuilder phone(String phone) { this.phone = phone; return this; }
        public AddressBuilder province(String province) { this.province = province; return this; }
        public AddressBuilder city(String city) { this.city = city; return this; }
        public AddressBuilder district(String district) { this.district = district; return this; }
        public AddressBuilder detail(String detail) { this.detail = detail; return this; }
        public AddressBuilder isDefault(Integer isDefault) { this.isDefault = isDefault; return this; }
        public AddressBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AddressBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Address build() {
            Address a = new Address();
            a.id = this.id;
            a.userId = this.userId;
            a.receiverName = this.receiverName;
            a.phone = this.phone;
            a.province = this.province;
            a.city = this.city;
            a.district = this.district;
            a.detail = this.detail;
            a.isDefault = this.isDefault;
            a.createdAt = this.createdAt;
            a.updatedAt = this.updatedAt;
            return a;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
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
