package com.campusdeal.controller;

import com.campusdeal.common.Result;
import com.campusdeal.dto.request.CartRequest;
import com.campusdeal.entity.CartItem;
import com.campusdeal.service.CartService;
import com.campusdeal.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartService cartService;
    private final JwtUtil jwtUtil;
    public CartController(CartService cartService, JwtUtil jwtUtil) {
        this.cartService = cartService;
        this.jwtUtil = jwtUtil;
    }


    @GetMapping
    public Result<List<CartItem>> list(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(cartService.getCartItems(userId));
    }

    @PostMapping
    public Result<CartItem> add(@RequestHeader("Authorization") String authHeader,
                                 @Valid @RequestBody CartRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(cartService.addToCart(userId, request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/{id}")
    public Result<CartItem> updateQty(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable Long id,
                                       @RequestBody Map<String, Integer> body) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(cartService.updateQuantity(userId, id, body.get("quantity")));
    }

    @PutMapping("/{id}/select")
    public Result<?> toggleSelect(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        cartService.toggleSelect(userId, id);
        return Result.success(null);
    }

    @PutMapping("/select-all")
    public Result<?> selectAll(@RequestHeader("Authorization") String authHeader,
                                @RequestBody Map<String, Boolean> body) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        cartService.toggleSelectAll(userId, body.get("selectAll"));
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<?> remove(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        cartService.removeCartItem(userId, id);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    public Result<?> batchRemove(@RequestHeader("Authorization") String authHeader,
                                  @RequestBody Map<String, List<Long>> body) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        cartService.batchRemove(userId, body.get("productIds"));
        return Result.success(null);
    }
}
