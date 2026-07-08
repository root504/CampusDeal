package com.campusdeal.controller.admin;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.dto.request.ProductRequest;
import com.campusdeal.entity.Product;
import com.campusdeal.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {
    private final ProductService productService;
    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping
    public Result<PageResult<Product>> list(@RequestParam(required = false) Integer status,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "8") int size) {
        return Result.success(productService.getAdminProducts(status, keyword, page, size));
    }

    /** 管理员编辑商品信息 */
    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return Result.success(productService.adminUpdateProduct(id, request));
    }

    /** 管理员删除商品（物理删除，同时清理关联数据） */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        productService.adminDeleteProduct(id);
        return Result.success(null);
    }

    /** 管理员软删除商品（status=4，已删除） */
    @PutMapping("/{id}/soft-delete")
    public Result<?> softDelete(@PathVariable Long id) {
        productService.adminDeleteProduct(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/audit")
    public Result<?> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer status = (Integer) body.get("status");
        String remark = (String) body.get("remark");
        productService.auditProduct(id, status, remark);
        return Result.success(null);
    }

    /** 审核通过 */
    @PutMapping("/{id}/approve")
    public Result<?> approve(@PathVariable Long id) {
        productService.approveProduct(id);
        return Result.success(null);
    }

    /** 审核驳回 */
    @PutMapping("/{id}/reject")
    public Result<?> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        productService.rejectProduct(id, reason);
        return Result.success(null);
    }

    @PutMapping("/{id}/off-shelf")
    public Result<?> offShelf(@PathVariable Long id) {
        productService.offShelfProduct(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/on-shelf")
    public Result<?> onShelf(@PathVariable Long id) {
        productService.onShelfProduct(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/appeal/approve")
    public Result<?> approveAppeal(@PathVariable Long id) {
        productService.approveAppeal(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/appeal/reject")
    public Result<?> rejectAppeal(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String remark = body.getOrDefault("remark", "");
        productService.rejectAppeal(id, remark);
        return Result.success(null);
    }
}
