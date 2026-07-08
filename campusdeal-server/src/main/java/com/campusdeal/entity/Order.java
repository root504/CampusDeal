package com.campusdeal.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "ship_time")
    private LocalDateTime shipTime;

    @Column(name = "receive_time")
    private LocalDateTime receiveTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private List<OrderItem> items;

    @Transient
    private User buyer;

    @Transient
    private User seller;

    public Order() {}

    public Order(Long id, String orderNo, Long buyerId, Long sellerId, BigDecimal totalAmount,
                 String status, LocalDateTime paymentTime, LocalDateTime shipTime,
                 LocalDateTime receiveTime, LocalDateTime completeTime, LocalDateTime cancelTime,
                 String cancelReason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.orderNo = orderNo; this.buyerId = buyerId;
        this.sellerId = sellerId; this.totalAmount = totalAmount;
        this.status = status; this.paymentTime = paymentTime;
        this.shipTime = shipTime; this.receiveTime = receiveTime;
        this.completeTime = completeTime; this.cancelTime = cancelTime;
        this.cancelReason = cancelReason; this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrderBuilder builder() { return new OrderBuilder(); }

    public static class OrderBuilder {
        private Long id;
        private String orderNo;
        private Long buyerId;
        private Long sellerId;
        private BigDecimal totalAmount;
        private String status = "pending";
        private LocalDateTime paymentTime;
        private LocalDateTime shipTime;
        private LocalDateTime receiveTime;
        private LocalDateTime completeTime;
        private LocalDateTime cancelTime;
        private String cancelReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        OrderBuilder() {}
        public OrderBuilder id(Long id) { this.id = id; return this; }
        public OrderBuilder orderNo(String orderNo) { this.orderNo = orderNo; return this; }
        public OrderBuilder buyerId(Long buyerId) { this.buyerId = buyerId; return this; }
        public OrderBuilder sellerId(Long sellerId) { this.sellerId = sellerId; return this; }
        public OrderBuilder totalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; return this; }
        public OrderBuilder status(String status) { this.status = status; return this; }
        public OrderBuilder paymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; return this; }
        public OrderBuilder shipTime(LocalDateTime shipTime) { this.shipTime = shipTime; return this; }
        public OrderBuilder receiveTime(LocalDateTime receiveTime) { this.receiveTime = receiveTime; return this; }
        public OrderBuilder completeTime(LocalDateTime completeTime) { this.completeTime = completeTime; return this; }
        public OrderBuilder cancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; return this; }
        public OrderBuilder cancelReason(String cancelReason) { this.cancelReason = cancelReason; return this; }
        public OrderBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public OrderBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Order build() {
            Order o = new Order();
            o.id = this.id; o.orderNo = this.orderNo; o.buyerId = this.buyerId;
            o.sellerId = this.sellerId; o.totalAmount = this.totalAmount;
            o.status = this.status; o.paymentTime = this.paymentTime;
            o.shipTime = this.shipTime; o.receiveTime = this.receiveTime;
            o.completeTime = this.completeTime; o.cancelTime = this.cancelTime;
            o.cancelReason = this.cancelReason; o.createdAt = this.createdAt;
            o.updatedAt = this.updatedAt;
            return o;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    public LocalDateTime getShipTime() { return shipTime; }
    public void setShipTime(LocalDateTime shipTime) { this.shipTime = shipTime; }
    public LocalDateTime getReceiveTime() { return receiveTime; }
    public void setReceiveTime(LocalDateTime receiveTime) { this.receiveTime = receiveTime; }
    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }
    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "pending";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
