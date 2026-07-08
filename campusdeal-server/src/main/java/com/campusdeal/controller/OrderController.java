package com.campusdeal.controller;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.entity.Order;
import com.campusdeal.repository.OrderRepository;
import com.campusdeal.service.OrderService;
import com.campusdeal.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;
    private final JwtUtil jwtUtil;
    private final OrderRepository orderRepository;
    public OrderController(OrderService orderService, JwtUtil jwtUtil, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
        this.orderRepository = orderRepository;
    }


    @PostMapping
    public Result<Order> create(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(orderService.createOrder(userId));
    }

    @GetMapping
    public Result<PageResult<Order>> list(@RequestHeader("Authorization") String authHeader,
                                           @RequestParam(defaultValue = "buyer") String role,
                                           @RequestParam(defaultValue = "all") String status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(orderService.getMyOrders(userId, role, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(orderService.getOrderDetail(userId, id));
    }

    @PutMapping("/{id}/pay")
    public Result<Order> pay(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(orderService.payOrder(userId, id));
    }

    @PutMapping("/{id}/ship")
    public Result<Order> ship(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(orderService.shipOrder(userId, id));
    }

    @PutMapping("/{id}/receive")
    public Result<Order> receive(@RequestHeader("Authorization") String authHeader, @PathVariable Long id) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(orderService.receiveOrder(userId, id));
    }

    @PutMapping("/{id}/cancel")
    public Result<Order> cancel(@RequestHeader("Authorization") String authHeader,
                                 @PathVariable Long id,
                                 @RequestBody(required = false) Map<String, String> body) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        String reason = body != null ? body.get("reason") : null;
        return Result.success(orderService.cancelOrder(userId, id, reason));
    }

    @PostMapping("/{id}/review")
    public Result<?> reviewOrder(@RequestHeader("Authorization") String authHeader,
                                  @PathVariable Long id,
                                  @RequestBody Map<String, Object> data) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        orderService.reviewOrder(userId, id, data);
        return Result.success(Map.of("message", "评价成功"));
    }

    /** 查询用户是否有待评价的已完成订单（按产品ID查） */
    @GetMapping("/reviewable")
    public Result<?> getReviewableOrder(@RequestHeader("Authorization") String authHeader,
                                         @RequestParam Long productId) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        List<Order> orders = orderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(userId, "completed",
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        for (Order o : orders) {
            boolean contains = orderService.getOrderDetail(userId, o.getId())
                    .getItems().stream().anyMatch(i -> i.getProductId().equals(productId));
            if (contains) {
                return Result.success(Map.of("orderId", o.getId(), "orderNo", o.getOrderNo()));
            }
        }
        return Result.success(null);
    }
}
