package com.campusdeal.service;

import com.campusdeal.common.PageResult;
import com.campusdeal.dto.request.AppealRequest;
import com.campusdeal.dto.request.ProductRequest;
import com.campusdeal.entity.Product;

public interface ProductService {
    PageResult<Product> listProducts(Integer categoryId, String keyword, int page, int size, String sort);
    Product getProductDetail(Long id);
    Product createProduct(Long userId, ProductRequest request);
    Product updateProduct(Long userId, Long productId, ProductRequest request);
    void deleteProduct(Long userId, Long productId);
    PageResult<Product> getHotProducts(int page, int size);
    PageResult<Product> getLatestProducts(int page, int size);
    PageResult<Product> getSimilarProducts(Long productId, int size);
    PageResult<Product> getMyProducts(Long userId, Integer status, int page, int size);
    Product getMyProductDetail(Long userId, Long productId);
    PageResult<Product> getAdminProducts(Integer status, String keyword, int page, int size);
    void auditProduct(Long productId, Integer status, String remark);
    void approveProduct(Long productId);
    void rejectProduct(Long productId, String reason);
    void offShelfProduct(Long productId);
    void onShelfProduct(Long productId);

    /** 管理员编辑商品信息（无需校验归属） */
    Product adminUpdateProduct(Long productId, ProductRequest request);

    /** 管理员删除商品（软删除，将状态置为 4=已删除） */
    void adminDeleteProduct(Long productId);

    /** 提交申诉 */
    void submitAppeal(Long userId, Long productId, AppealRequest request);

    /** 申诉通过 */
    void approveAppeal(Long productId);

    /** 申诉驳回 */
    void rejectAppeal(Long productId, String remark);
}
