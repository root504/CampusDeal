package com.campusdeal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_rule")
public class OrderRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    private String category = "cancel_order";

    @Column(name = "auto_cancel_minutes", nullable = false)
    private Integer autoCancelMinutes = 30;

    @Column(nullable = false)
    private Boolean applied = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public OrderRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getAutoCancelMinutes() { return autoCancelMinutes; }
    public void setAutoCancelMinutes(Integer autoCancelMinutes) { this.autoCancelMinutes = autoCancelMinutes; }
    public Boolean getApplied() { return applied; }
    public void setApplied(Boolean applied) { this.applied = applied; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
        if (applied == null) applied = false;
        if (category == null) category = "cancel_order";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
