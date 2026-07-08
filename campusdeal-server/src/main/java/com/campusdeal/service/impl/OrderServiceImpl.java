package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.common.PageResult;
import com.campusdeal.entity.*;
import com.campusdeal.repository.*;
import com.campusdeal.service.CreditService;
import com.campusdeal.service.MessageService;
import com.campusdeal.service.OrderService;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final MessageService messageService;
    private final ReviewRepository reviewRepository;
    private final CreditService creditService;
    private final CreditRuleRepository creditRuleRepository;
    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            CartItemRepository cartItemRepository, ProductRepository productRepository,
                            UserRepository userRepository, AddressRepository addressRepository,
                            MessageService messageService, ReviewRepository reviewRepository,
                            CreditService creditService, CreditRuleRepository creditRuleRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.messageService = messageService;
        this.reviewRepository = reviewRepository;
        this.creditService = creditService;
        this.creditRuleRepository = creditRuleRepository;
    }


    @Override
    @Transactional
    public Order createOrder(Long buyerId) {
        List<CartItem> selectedItems = cartItemRepository.findByUserIdAndSelected(buyerId, 1);
        if (selectedItems.isEmpty()) {
            throw new BusinessException("请选择要购买的商品");
        }

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        Long sellerId = null;

        for (CartItem item : selectedItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new BusinessException("商品不存在: " + item.getProductId()));

            if (product.getStatus() != 1) {
                throw new BusinessException("商品「" + product.getTitle() + "」已下架");
            }

            if (buyerId.equals(product.getSellerId())) {
                throw new BusinessException("不能购买自己发布的商品");
            }

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            if (sellerId == null) {
                sellerId = product.getSellerId();
            }
        }

        // 创建订单
        Order order = Order.builder()
                .orderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6))
                .buyerId(buyerId)
                .sellerId(sellerId)
                .totalAmount(totalAmount)
                .status("pending")
                .build();

        order = orderRepository.save(order);

        // 创建订单明细
        for (CartItem item : selectedItems) {
            Product product = productRepository.findById(item.getProductId()).get();
            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(product.getId())
                    .productTitle(product.getTitle())
                    .productImage(product.getCoverImage())
                    .price(product.getPrice())
                    .quantity(item.getQuantity())
                    .build();
            orderItemRepository.save(orderItem);
        }

        // 清空购物车中已下单的商品
        List<Long> productIds = selectedItems.stream().map(CartItem::getProductId).toList();
        cartItemRepository.deleteByUserIdAndProductIdIn(buyerId, productIds);

        return order;
    }

    @Override
    public PageResult<Order> getMyOrders(Long userId, String role, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage;

        boolean hasStatus = status != null && !status.isBlank() && !"all".equals(status);

        if ("seller".equals(role)) {
            // 我卖出的：按 sellerId 查询
            if (hasStatus) {
                orderPage = orderRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
            } else {
                orderPage = orderRepository.findBySellerIdOrderByCreatedAtDesc(userId, pageable);
            }
        } else if ("all".equals(role)) {
            // 全部（我买入的 + 我卖出的）
            if (hasStatus) {
                orderPage = orderRepository.findByBuyerIdOrSellerIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
            } else {
                orderPage = orderRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, pageable);
            }
        } else {
            // 默认：我买入的（buyer），保持向后兼容
            if (hasStatus) {
                orderPage = orderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
            } else {
                orderPage = orderRepository.findByBuyerIdOrderByCreatedAtDesc(userId, pageable);
            }
        }

        // 批量加载订单商品项
        List<Order> orders = orderPage.getContent();
        if (!orders.isEmpty()) {
            List<Long> orderIds = orders.stream().map(Order::getId).toList();
            List<OrderItem> allItems = orderItemRepository.findByOrderIdIn(orderIds);
            java.util.Map<Long, List<OrderItem>> itemsMap = allItems.stream()
                    .collect(java.util.stream.Collectors.groupingBy(OrderItem::getOrderId));
            orders.forEach(o -> o.setItems(itemsMap.getOrDefault(o.getId(), List.of())));

            // 批量加载买家/卖家昵称
            List<Long> userIds = orders.stream()
                    .flatMap(o -> Stream.of(o.getBuyerId(), o.getSellerId()))
                    .distinct().toList();
            Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            orders.forEach(o -> {
                o.setBuyer(userMap.get(o.getBuyerId()));
                o.setSeller(userMap.get(o.getSellerId()));
            });
        }

        return PageResult.of(orders, orderPage.getTotalElements(), page, size);
    }

    @Override
    public Order getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }

        // 加载订单商品项
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        order.setItems(items);

        // 加载买家/卖家昵称
        User buyer = userRepository.findById(order.getBuyerId()).orElse(null);
        User seller = userRepository.findById(order.getSellerId()).orElse(null);
        order.setBuyer(buyer);
        order.setSeller(seller);

        return order;
    }

    @Override
    @Transactional
    public Order payOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (!"pending".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许付款");
        }

        // 检查买家是否有收货地址
        long addressCount = addressRepository.countByUserId(userId);
        if (addressCount == 0) {
            throw new BusinessException("请先添加收货地址");
        }

        order.setStatus("paid");
        order.setPaymentTime(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order shipOrder(Long sellerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作");
        }

        if (!"paid".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许发货");
        }

        order.setStatus("shipped");
        order.setShipTime(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order receiveOrder(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (!"shipped".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许确认收货");
        }

        order.setStatus("completed");
        order.setReceiveTime(LocalDateTime.now());
        order.setCompleteTime(LocalDateTime.now());
        order = orderRepository.save(order);

        // 通知卖家：买家已确认收货
        messageService.sendSystemMessage(order.getSellerId(),
            "买家已确认收货，订单 " + order.getOrderNo() + " 交易完成。", "order", orderId);

        // 成功交易：给买家加信誉分
        addCreditForCompletedTrade(order.getBuyerId());

        return order;
    }

    @Override
    @Transactional
    public Order cancelOrder(Long userId, Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (!"pending".equals(order.getStatus()) && !"paid".equals(order.getStatus())) {
            throw new BusinessException("仅待付款或已付款未发货状态可取消订单");
        }

        order.setStatus("cancelled");
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        return orderRepository.save(order);
    }

    @Override
    public PageResult<Order> getAdminOrders(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Order> orderPage;

        if (status != null && !status.isBlank()) {
            orderPage = orderRepository.findByStatus(status, pageable);
        } else {
            // 默认排除已删除订单
            orderPage = orderRepository.findByStatusNot("deleted", pageable);
        }

        // 加载订单商品项 + 买家/卖家信息
        List<Order> orders = orderPage.getContent();
        if (!orders.isEmpty()) {
            List<Long> orderIds = orders.stream().map(Order::getId).toList();
            List<OrderItem> allItems = orderItemRepository.findByOrderIdIn(orderIds);
            Map<Long, List<OrderItem>> itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(OrderItem::getOrderId));
            orders.forEach(o -> o.setItems(itemsMap.getOrDefault(o.getId(), List.of())));

            List<Long> userIds = orders.stream()
                .flatMap(o -> java.util.stream.Stream.of(o.getBuyerId(), o.getSellerId()))
                .distinct().toList();
            Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
            orders.forEach(o -> {
                o.setBuyer(userMap.get(o.getBuyerId()));
                o.setSeller(userMap.get(o.getSellerId()));
            });
        }

        return PageResult.of(orders, orderPage.getTotalElements(), page, size);
    }

    @Override
    public Map<String, Object> getOrderStats() {
        long totalOrders = orderRepository.count();
        long pendingShip = orderRepository.countByStatus("paid");
        long completed = orderRepository.countByStatus("completed");
        BigDecimal totalAmount = orderRepository.totalTransactionAmount().orElse(BigDecimal.ZERO);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("pendingShip", pendingShip);
        stats.put("completed", completed);
        stats.put("totalAmount", totalAmount);
        return stats;
    }

    @Override
    @Transactional
    public void reviewOrder(Long userId, Long orderId, java.util.Map<String, Object> data) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权评价该订单");
        }

        if (!"completed".equals(order.getStatus())) {
            throw new BusinessException("仅已完成的订单可以评价");
        }

        // 检查是否已评价
        if (reviewRepository.existsByOrderIdAndBuyerId(orderId, userId)) {
            throw new BusinessException("该订单已评价");
        }

        // 获取订单商品（取第一个商品的 productId）
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) {
            throw new BusinessException("订单无商品，无法评价");
        }
        Long productId = items.get(0).getProductId();

        // 验证评分
        Integer rating = data != null && data.containsKey("rating")
                ? Integer.valueOf(data.get("rating").toString()) : null;
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }

        String content = data != null && data.containsKey("content")
                ? (String) data.get("content") : "";

        // 创建评价
        Review review = Review.builder()
                .orderId(orderId)
                .productId(productId)
                .userId(userId)
                .buyerId(userId)
                .sellerId(order.getSellerId())
                .rating(rating)
                .content(content)
                .build();
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void adminShipOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!"paid".equals(order.getStatus())) {
            throw new BusinessException("仅待发货订单可以发货");
        }
        order.setStatus("shipped");
        order.setShipTime(LocalDateTime.now());
        orderRepository.save(order);

        // 通知买家
        messageService.sendSystemMessage(order.getBuyerId(),
            "您的订单 " + order.getOrderNo() + " 已发货，请注意查收。", "order", orderId);
    }

    @Override
    @Transactional
    public void adminReceiveOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!"shipped".equals(order.getStatus())) {
            throw new BusinessException("仅待收货订单可以确认收货");
        }
        order.setStatus("completed");
        order.setReceiveTime(LocalDateTime.now());
        order.setCompleteTime(LocalDateTime.now());
        orderRepository.save(order);

        // 通知买家
        messageService.sendSystemMessage(order.getBuyerId(),
            "您的订单 " + order.getOrderNo() + " 已确认收货，交易完成。", "order", orderId);

        // 成功交易：给买家加信誉分
        addCreditForCompletedTrade(order.getBuyerId());
    }

    @Override
    public void adminRemindPayment(Long orderId, String message) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!"pending".equals(order.getStatus())) {
            throw new BusinessException("仅待付款订单可以催缴");
        }

        // 通知买家付款（支持自定义消息）
        String content = (message != null && !message.isBlank())
                ? message
                : "您的订单 " + order.getOrderNo() + " 尚未付款，请尽快完成支付。";
        messageService.sendSystemMessage(order.getBuyerId(), content, "order", orderId);
    }

    @Override
    public Order getAdminOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        // 加载订单商品项
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        order.setItems(items);

        // 加载买家/卖家昵称
        User buyer = userRepository.findById(order.getBuyerId()).orElse(null);
        User seller = userRepository.findById(order.getSellerId()).orElse(null);
        order.setBuyer(buyer);
        order.setSeller(seller);

        return order;
    }

    @Override
    @Transactional
    public void adminDeleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        order.setStatus("deleted");
        orderRepository.save(order);
    }

    /**
     * 成功交易后给买家加信誉分。
     * 从 credit_rules 表查询已应用的"成功交易"奖励规则，按规则分值加分。
     */
    private void addCreditForCompletedTrade(Long buyerId) {
        List<CreditRule> appliedRules = creditRuleRepository.findByAppliedTrueOrderBySortOrderAsc();
        for (CreditRule rule : appliedRules) {
            if ("bonus".equals(rule.getCategory()) && rule.getDescription() != null
                    && rule.getDescription().contains("成功交易")) {
                int score = rule.getScore();
                if (score > 0) {
                    creditService.changeCredit(buyerId, score,
                            "成功交易 +" + score + " 分", null);
                }
                break;
            }
        }
    }
}
