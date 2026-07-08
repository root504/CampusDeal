package com.campusdeal.service;

import com.campusdeal.entity.CartItem;

import java.util.List;

public interface CartService {
    List<CartItem> getCartItems(Long userId);
    CartItem addToCart(Long userId, Long productId, Integer quantity);
    CartItem updateQuantity(Long userId, Long cartItemId, Integer quantity);
    void toggleSelect(Long userId, Long cartItemId);
    void toggleSelectAll(Long userId, Boolean selectAll);
    void removeCartItem(Long userId, Long cartItemId);
    void batchRemove(Long userId, List<Long> productIds);
}
