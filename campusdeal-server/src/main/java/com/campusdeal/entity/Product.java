package com.campusdeal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "condition_level", nullable = false, length = 20)
    private String conditionLevel;

    @Column(length = 50)
    private String campus;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Transient
    private List<ProductImage> images = new ArrayList<>();

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    private Integer status = 0;

    @Column(name = "audit_remark", length = 255)
    private String auditRemark;

    @Column(name = "appeal_reason", columnDefinition = "TEXT")
    private String appealReason;

    @Column(name = "appeal_status")
    private Integer appealStatus = 0;

    @Column(name = "appeal_remark", length = 500)
    private String appealRemark;

    @Column(name = "appeal_time")
    private LocalDateTime appealTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Product() {}

    public Product(Long id, Long sellerId, User seller, Integer categoryId, Category category,
                   String title, String description, BigDecimal price, BigDecimal originalPrice,
                   String conditionLevel, String campus, String coverImage, Integer viewCount,
                   Integer favoriteCount, Integer status, String auditRemark,
                   String appealReason, Integer appealStatus, String appealRemark, LocalDateTime appealTime,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.seller = seller;
        this.categoryId = categoryId;
        this.category = category;
        this.title = title;
        this.description = description;
        this.price = price;
        this.originalPrice = originalPrice;
        this.conditionLevel = conditionLevel;
        this.campus = campus;
        this.coverImage = coverImage;
        this.viewCount = viewCount;
        this.favoriteCount = favoriteCount;
        this.status = status;
        this.auditRemark = auditRemark;
        this.appealReason = appealReason;
        this.appealStatus = appealStatus;
        this.appealRemark = appealRemark;
        this.appealTime = appealTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductBuilder builder() { return new ProductBuilder(); }

    public static class ProductBuilder {
        private Long id;
        private Long sellerId;
        private User seller;
        private Integer categoryId;
        private Category category;
        private String title;
        private String description;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private String conditionLevel;
        private String campus;
        private String coverImage;
        private Integer viewCount = 0;
        private Integer favoriteCount = 0;
        private Integer status = 0;
        private String auditRemark;
        private String appealReason;
        private Integer appealStatus = 0;
        private String appealRemark;
        private LocalDateTime appealTime;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        ProductBuilder() {}
        public ProductBuilder id(Long id) { this.id = id; return this; }
        public ProductBuilder sellerId(Long sellerId) { this.sellerId = sellerId; return this; }
        public ProductBuilder seller(User seller) { this.seller = seller; return this; }
        public ProductBuilder categoryId(Integer categoryId) { this.categoryId = categoryId; return this; }
        public ProductBuilder category(Category category) { this.category = category; return this; }
        public ProductBuilder title(String title) { this.title = title; return this; }
        public ProductBuilder description(String description) { this.description = description; return this; }
        public ProductBuilder price(BigDecimal price) { this.price = price; return this; }
        public ProductBuilder originalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; return this; }
        public ProductBuilder conditionLevel(String conditionLevel) { this.conditionLevel = conditionLevel; return this; }
        public ProductBuilder campus(String campus) { this.campus = campus; return this; }
        public ProductBuilder coverImage(String coverImage) { this.coverImage = coverImage; return this; }
        public ProductBuilder viewCount(Integer viewCount) { this.viewCount = viewCount; return this; }
        public ProductBuilder favoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; return this; }
        public ProductBuilder status(Integer status) { this.status = status; return this; }
        public ProductBuilder auditRemark(String auditRemark) { this.auditRemark = auditRemark; return this; }
        public ProductBuilder appealReason(String appealReason) { this.appealReason = appealReason; return this; }
        public ProductBuilder appealStatus(Integer appealStatus) { this.appealStatus = appealStatus; return this; }
        public ProductBuilder appealRemark(String appealRemark) { this.appealRemark = appealRemark; return this; }
        public ProductBuilder appealTime(LocalDateTime appealTime) { this.appealTime = appealTime; return this; }
        public ProductBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ProductBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Product build() {
            Product p = new Product();
            p.id = this.id; p.sellerId = this.sellerId; p.seller = this.seller;
            p.categoryId = this.categoryId; p.category = this.category;
            p.title = this.title; p.description = this.description;
            p.price = this.price; p.originalPrice = this.originalPrice;
            p.conditionLevel = this.conditionLevel; p.campus = this.campus;
            p.coverImage = this.coverImage; p.viewCount = this.viewCount;
            p.favoriteCount = this.favoriteCount; p.status = this.status;
            p.auditRemark = this.auditRemark;
            p.appealReason = this.appealReason; p.appealStatus = this.appealStatus;
            p.appealRemark = this.appealRemark; p.appealTime = this.appealTime;
            p.createdAt = this.createdAt; p.updatedAt = this.updatedAt;
            return p;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public String getConditionLevel() { return conditionLevel; }
    public void setConditionLevel(String conditionLevel) { this.conditionLevel = conditionLevel; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getAppealReason() { return appealReason; }
    public void setAppealReason(String appealReason) { this.appealReason = appealReason; }
    public Integer getAppealStatus() { return appealStatus; }
    public void setAppealStatus(Integer appealStatus) { this.appealStatus = appealStatus; }
    public String getAppealRemark() { return appealRemark; }
    public void setAppealRemark(String appealRemark) { this.appealRemark = appealRemark; }
    public LocalDateTime getAppealTime() { return appealTime; }
    public void setAppealTime(LocalDateTime appealTime) { this.appealTime = appealTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = 0;
        if (viewCount == null) viewCount = 0;
        if (favoriteCount == null) favoriteCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
