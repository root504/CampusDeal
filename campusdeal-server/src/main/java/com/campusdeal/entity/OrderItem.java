package com.campusdeal.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_title", nullable = false, length = 200)
    private String productTitle;

    @Column(name = "product_image", length = 500)
    private String productImage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrderItem() {}

    public OrderItem(Long id, Long orderId, Long productId, String productTitle,
                     String productImage, BigDecimal price, Integer quantity,
                     LocalDateTime createdAt) {
        this.id = id; this.orderId = orderId; this.productId = productId;
        this.productTitle = productTitle; this.productImage = productImage;
        this.price = price; this.quantity = quantity; this.createdAt = createdAt;
    }

    public static OrderItemBuilder builder() { return new OrderItemBuilder(); }

    public static class OrderItemBuilder {
        private Long id;
        private Long orderId;
        private Long productId;
        private String productTitle;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private LocalDateTime createdAt;
        OrderItemBuilder() {}
        public OrderItemBuilder id(Long id) { this.id = id; return this; }
        public OrderItemBuilder orderId(Long orderId) { this.orderId = orderId; return this; }
        public OrderItemBuilder productId(Long productId) { this.productId = productId; return this; }
        public OrderItemBuilder productTitle(String productTitle) { this.productTitle = productTitle; return this; }
        public OrderItemBuilder productImage(String productImage) { this.productImage = productImage; return this; }
        public OrderItemBuilder price(BigDecimal price) { this.price = price; return this; }
        public OrderItemBuilder quantity(Integer quantity) { this.quantity = quantity; return this; }
        public OrderItemBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderItem build() {
            OrderItem o = new OrderItem();
            o.id = this.id; o.orderId = this.orderId; o.productId = this.productId;
            o.productTitle = this.productTitle; o.productImage = this.productImage;
            o.price = this.price; o.quantity = this.quantity; o.createdAt = this.createdAt;
            return o;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
