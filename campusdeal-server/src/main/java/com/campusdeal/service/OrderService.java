package com.campusdeal.service;

import com.campusdeal.common.PageResult;
import com.campusdeal.entity.Order;

public interface OrderService {
    Order createOrder(Long buyerId);
    PageResult<Order> getMyOrders(Long userId, String role, String status, int page, int size);
    Order getOrderDetail(Long userId, Long orderId);
    Order payOrder(Long userId, Long orderId);
    Order shipOrder(Long sellerId, Long orderId);
    Order receiveOrder(Long userId, Long orderId);
    Order cancelOrder(Long userId, Long orderId, String reason);
    void reviewOrder(Long userId, Long orderId, java.util.Map<String, Object> data);
    PageResult<Order> getAdminOrders(String status, int page, int size);
    Order getAdminOrderDetail(Long orderId);
    java.util.Map<String, Object> getOrderStats();
    void adminShipOrder(Long orderId);
    void adminReceiveOrder(Long orderId);
    void adminRemindPayment(Long orderId, String message);
    void adminDeleteOrder(Long orderId);
}
