package com.campusdeal.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Favorite() {}

    public Favorite(Long id, Long userId, Long productId, Product product, LocalDateTime createdAt) {
        this.id = id; this.userId = userId; this.productId = productId;
        this.product = product; this.createdAt = createdAt;
    }

    public static FavoriteBuilder builder() { return new FavoriteBuilder(); }

    public static class FavoriteBuilder {
        private Long id;
        private Long userId;
        private Long productId;
        private Product product;
        private LocalDateTime createdAt;
        FavoriteBuilder() {}
        public FavoriteBuilder id(Long id) { this.id = id; return this; }
        public FavoriteBuilder userId(Long userId) { this.userId = userId; return this; }
        public FavoriteBuilder productId(Long productId) { this.productId = productId; return this; }
        public FavoriteBuilder product(Product product) { this.product = product; return this; }
        public FavoriteBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Favorite build() {
            Favorite f = new Favorite();
            f.id = this.id; f.userId = this.userId; f.productId = this.productId;
            f.product = this.product; f.createdAt = this.createdAt;
            return f;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
