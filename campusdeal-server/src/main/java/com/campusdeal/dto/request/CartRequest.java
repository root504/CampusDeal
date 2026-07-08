package com.campusdeal.dto.request;

import jakarta.validation.constraints.NotNull;

public class CartRequest {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private Integer quantity = 1;

    public CartRequest() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
