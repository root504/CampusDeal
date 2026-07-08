package com.campusdeal.controller;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.entity.Review;
import com.campusdeal.repository.OrderItemRepository;
import com.campusdeal.repository.ReviewRepository;
import com.campusdeal.repository.UserRepository;
import com.campusdeal.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final JwtUtil jwtUtil;

    public ReviewController(ReviewRepository reviewRepository, UserRepository userRepository,
                            OrderItemRepository orderItemRepository, JwtUtil jwtUtil) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.jwtUtil = jwtUtil;
    }

    /** 查询某商品的评价列表 */
    @GetMapping("/product/{productId}")
    public Result<PageResult<Review>> productReviews(@PathVariable Long productId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        var pageResult = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId,
                org.springframework.data.domain.PageRequest.of(page - 1, size));
        // 填充买家昵称和订单商品名称
        pageResult.getContent().forEach(r -> {
            userRepository.findById(r.getBuyerId()).ifPresent(u -> r.setBuyerName(u.getUsername()));
            orderItemRepository.findByOrderId(r.getOrderId()).stream()
                    .findFirst()
                    .ifPresent(item -> r.setOrderProductName(item.getProductTitle()));
        });
        return Result.success(PageResult.of(pageResult.getContent(), pageResult.getTotalElements(), page, size));
    }

    /** 查询某卖家收到的评价总人数（去重 buyerId） */
    @GetMapping("/seller/{sellerId}/count")
    public Result<Map<String, Object>> sellerReviewCount(@PathVariable Long sellerId) {
        long count = reviewRepository.countDistinctBuyersBySellerId(sellerId);
        return Result.success(Map.of("count", count));
    }

    /** 检查某订单是否已评价 */
    @GetMapping("/check")
    public Result<Map<String, Object>> checkReviewed(@RequestHeader("Authorization") String authHeader,
                                                      @RequestParam Long orderId) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        boolean exists = reviewRepository.existsByOrderIdAndBuyerId(orderId, userId);
        return Result.success(Map.of("reviewed", exists));
    }
}
