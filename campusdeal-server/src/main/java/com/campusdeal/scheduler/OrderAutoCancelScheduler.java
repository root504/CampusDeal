package com.campusdeal.scheduler;

import com.campusdeal.entity.Order;
import com.campusdeal.entity.OrderRule;
import com.campusdeal.repository.OrderRepository;
import com.campusdeal.repository.OrderRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderAutoCancelScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCancelScheduler.class);

    private final OrderRepository orderRepository;
    private final OrderRuleRepository orderRuleRepository;

    public OrderAutoCancelScheduler(OrderRepository orderRepository,
                                    OrderRuleRepository orderRuleRepository) {
        this.orderRepository = orderRepository;
        this.orderRuleRepository = orderRuleRepository;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void autoCancelExpiredOrders() {
        List<OrderRule> appliedRules = orderRuleRepository.findByAppliedTrue();
        OrderRule rule = appliedRules.isEmpty()
                ? orderRuleRepository.findAll().stream().findFirst().orElse(null)
                : appliedRules.get(0);

        int minutes = (rule != null && rule.getAutoCancelMinutes() != null)
                ? rule.getAutoCancelMinutes() : 30;

        LocalDateTime deadline = LocalDateTime.now().minusMinutes(minutes);

        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore("pending", deadline);

        if (expiredOrders.isEmpty()) {
            return;
        }

        for (Order order : expiredOrders) {
            order.setStatus("cancelled");
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason("超时未支付，系统自动取消（" + minutes + "分钟）");
        }

        orderRepository.saveAll(expiredOrders);
        log.info("自动取消 {} 条超时未支付订单（规则：{} 分钟）", expiredOrders.size(), minutes);
    }
}
