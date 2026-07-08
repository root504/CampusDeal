package com.campusdeal.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Transient
    private String buyerName;

    @Transient
    private String orderProductName;

    public Review() {}

    public static ReviewBuilder builder() { return new ReviewBuilder(); }

    public static class ReviewBuilder {
        private Long id;
        private Long orderId;
        private Long productId;
        private Long userId;
        private Long buyerId;
        private Long sellerId;
        private Integer rating;
        private String content;
        private LocalDateTime createdAt;

        ReviewBuilder() {}
        public ReviewBuilder id(Long id) { this.id = id; return this; }
        public ReviewBuilder orderId(Long orderId) { this.orderId = orderId; return this; }
        public ReviewBuilder productId(Long productId) { this.productId = productId; return this; }
        public ReviewBuilder userId(Long userId) { this.userId = userId; return this; }
        public ReviewBuilder buyerId(Long buyerId) { this.buyerId = buyerId; return this; }
        public ReviewBuilder sellerId(Long sellerId) { this.sellerId = sellerId; return this; }
        public ReviewBuilder rating(Integer rating) { this.rating = rating; return this; }
        public ReviewBuilder content(String content) { this.content = content; return this; }
        public ReviewBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Review build() {
            Review r = new Review();
            r.id = this.id; r.orderId = this.orderId; r.productId = this.productId;
            r.userId = this.userId; r.buyerId = this.buyerId; r.sellerId = this.sellerId;
            r.rating = this.rating; r.content = this.content; r.createdAt = this.createdAt;
            return r;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getOrderProductName() { return orderProductName; }
    public void setOrderProductName(String orderProductName) { this.orderProductName = orderProductName; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
