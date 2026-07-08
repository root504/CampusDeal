package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.entity.CartItem;
import com.campusdeal.entity.Product;
import com.campusdeal.repository.CartItemRepository;
import com.campusdeal.repository.ProductRepository;
import com.campusdeal.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    public CartServiceImpl(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        // 触发懒加载的 product 关联，避免序列化时 product 为 null
        for (CartItem item : items) {
            if (item.getProduct() != null) {
                item.getProduct().getTitle();
                if (item.getProduct().getSeller() != null) item.getProduct().getSeller().getUsername();
            }
        }
        return items;
    }

    @Override
    @Transactional
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (product.getStatus() != 1) {
            throw new BusinessException("该商品已下架或售出");
        }

        if (userId.equals(product.getSellerId())) {
            throw new BusinessException("不能购买自己发布的商品");
        }

        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + (quantity != null ? quantity : 1));
            return cartItemRepository.save(item);
        }

        CartItem cartItem = CartItem.builder()
                .userId(userId)
                .productId(productId)
                .quantity(quantity != null ? quantity : 1)
                .selected(1)
                .build();

        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("购物车项不存在"));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public void toggleSelect(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("购物车项不存在"));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        item.setSelected(item.getSelected() == 1 ? 0 : 1);
        cartItemRepository.save(item);
    }

    @Override
    @Transactional
    public void toggleSelectAll(Long userId, Boolean selectAll) {
        cartItemRepository.updateSelectAll(userId, selectAll ? 1 : 0);
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException("购物车项不存在"));

        if (!item.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        cartItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void batchRemove(Long userId, List<Long> productIds) {
        cartItemRepository.deleteByUserIdAndProductIdIn(userId, productIds);
    }
}
