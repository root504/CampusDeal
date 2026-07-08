package com.campusdeal.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ProductImage() {}

    public ProductImage(Long id, Long productId, String url, Integer sortOrder, LocalDateTime createdAt) {
        this.id = id; this.productId = productId; this.url = url;
        this.sortOrder = sortOrder; this.createdAt = createdAt;
    }

    public static ProductImageBuilder builder() { return new ProductImageBuilder(); }

    public static class ProductImageBuilder {
        private Long id;
        private Long productId;
        private String url;
        private Integer sortOrder = 0;
        private LocalDateTime createdAt;
        ProductImageBuilder() {}
        public ProductImageBuilder id(Long id) { this.id = id; return this; }
        public ProductImageBuilder productId(Long productId) { this.productId = productId; return this; }
        public ProductImageBuilder url(String url) { this.url = url; return this; }
        public ProductImageBuilder sortOrder(Integer sortOrder) { this.sortOrder = sortOrder; return this; }
        public ProductImageBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ProductImage build() {
            ProductImage p = new ProductImage();
            p.id = this.id; p.productId = this.productId; p.url = this.url;
            p.sortOrder = this.sortOrder; p.createdAt = this.createdAt;
            return p;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
