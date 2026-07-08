package com.campusdeal.controller;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.dto.request.AppealRequest;
import com.campusdeal.dto.request.ProductRequest;
import com.campusdeal.entity.Product;
import com.campusdeal.service.ProductService;
import com.campusdeal.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;
    private final JwtUtil jwtUtil;
    public ProductController(ProductService productService, JwtUtil jwtUtil) {
        this.productService = productService;
        this.jwtUtil = jwtUtil;
    }


    @GetMapping
    public Result<PageResult<Product>> listProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort) {
        return Result.success(productService.listProducts(categoryId, keyword, page, size, sort));
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    @PostMapping
    public Result<Product> create(@RequestHeader("Authorization") String authHeader,
                                   @Valid @RequestBody ProductRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(productService.createProduct(userId, request));
    }

    @PutMapping("/{id}")
    public Result<Product> update(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable Long id,
                                   @Valid @RequestBody ProductRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(productService.updateProduct(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        productService.deleteProduct(userId, id);
        return Result.success(null);
    }

    @GetMapping("/hot")
    public Result<PageResult<Product>> hot(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return Result.success(productService.getHotProducts(page, size));
    }

    @GetMapping("/latest")
    public Result<PageResult<Product>> latest(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.success(productService.getLatestProducts(page, size));
    }

    @GetMapping("/{id}/similar")
    public Result<PageResult<Product>> similar(@PathVariable Long id,
                                                @RequestParam(defaultValue = "4") int size) {
        return Result.success(productService.getSimilarProducts(id, size));
    }

    @GetMapping("/my")
    public Result<PageResult<Product>> myProducts(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(productService.getMyProducts(userId, status, page, size));
    }

    @GetMapping("/my/{id}")
    public Result<Product> myProductDetail(@RequestHeader("Authorization") String authHeader,
                                            @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(productService.getMyProductDetail(userId, id));
    }

    @PostMapping("/{id}/appeal")
    public Result<?> submitAppeal(@RequestHeader("Authorization") String authHeader,
                                   @PathVariable Long id,
                                   @Valid @RequestBody AppealRequest request) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        productService.submitAppeal(userId, id, request);
        return Result.success(null);
    }
}
