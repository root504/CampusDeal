package com.campusdeal.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
public class CartItem {

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

    private Integer quantity = 1;

    private Integer selected = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CartItem() {}

    public CartItem(Long id, Long userId, Long productId, Product product, Integer quantity,
                    Integer selected, LocalDateTime createdAt) {
        this.id = id; this.userId = userId; this.productId = productId;
        this.product = product; this.quantity = quantity;
        this.selected = selected; this.createdAt = createdAt;
    }

    public static CartItemBuilder builder() { return new CartItemBuilder(); }

    public static class CartItemBuilder {
        private Long id;
        private Long userId;
        private Long productId;
        private Product product;
        private Integer quantity = 1;
        private Integer selected = 1;
        private LocalDateTime createdAt;
        CartItemBuilder() {}
        public CartItemBuilder id(Long id) { this.id = id; return this; }
        public CartItemBuilder userId(Long userId) { this.userId = userId; return this; }
        public CartItemBuilder productId(Long productId) { this.productId = productId; return this; }
        public CartItemBuilder product(Product product) { this.product = product; return this; }
        public CartItemBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public CartItemBuilder selected(Integer selected) { this.selected = selected; return this; }
        public CartItemBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public CartItem build() {
            CartItem c = new CartItem();
            c.id = this.id; c.userId = this.userId; c.productId = this.productId;
            c.product = this.product; c.quantity = this.quantity;
            c.selected = this.selected; c.createdAt = this.createdAt;
            return c;
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
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getSelected() { return selected; }
    public void setSelected(Integer selected) { this.selected = selected; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
