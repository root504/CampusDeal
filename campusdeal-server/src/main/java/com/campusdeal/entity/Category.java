package com.campusdeal.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, nullable = false, length = 30)
    private String code;

    @Column(name = "icon_class", length = 50)
    private String iconClass;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Category() {}

    public Category(Integer id, String name, String code, String iconClass, Integer sortOrder, LocalDateTime createdAt) {
        this.id = id; this.name = name; this.code = code;
        this.iconClass = iconClass; this.sortOrder = sortOrder; this.createdAt = createdAt;
    }

    public static CategoryBuilder builder() { return new CategoryBuilder(); }

    public static class CategoryBuilder {
        private Integer id;
        private String name;
        private String code;
        private String iconClass;
        private Integer sortOrder = 0;
        private LocalDateTime createdAt;
        CategoryBuilder() {}
        public CategoryBuilder id(Integer id) { this.id = id; return this; }
        public CategoryBuilder name(String name) { this.name = name; return this; }
        public CategoryBuilder code(String code) { this.code = code; return this; }
        public CategoryBuilder iconClass(String iconClass) { this.iconClass = iconClass; return this; }
        public CategoryBuilder sortOrder(Integer sortOrder) { this.sortOrder = sortOrder; return this; }
        public CategoryBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Category build() {
            Category c = new Category();
            c.id = this.id; c.name = this.name; c.code = this.code;
            c.iconClass = this.iconClass;
            c.sortOrder = this.sortOrder; c.createdAt = this.createdAt;
            return c;
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
