package com.campusdeal.controller.admin;

import com.campusdeal.common.Result;
import com.campusdeal.entity.OrderRule;
import com.campusdeal.repository.OrderRuleRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/order-rule")
public class AdminOrderRuleController {

    private final OrderRuleRepository orderRuleRepository;

    public AdminOrderRuleController(OrderRuleRepository orderRuleRepository) {
        this.orderRuleRepository = orderRuleRepository;
    }

    @GetMapping
    public Result<List<OrderRule>> list() {
        return Result.success(orderRuleRepository.findAllByOrderByUpdatedAtDesc());
    }

    @PostMapping
    public Result<OrderRule> create(@RequestBody OrderRule rule) {
        return Result.success(orderRuleRepository.save(rule));
    }

    @PutMapping("/{id}")
    public Result<OrderRule> update(@PathVariable Long id, @RequestBody OrderRule rule) {
        OrderRule existing = orderRuleRepository.findById(id).orElseThrow(() -> new RuntimeException("规则不存在"));
        existing.setDescription(rule.getDescription());
        existing.setCategory(rule.getCategory());
        existing.setAutoCancelMinutes(rule.getAutoCancelMinutes());
        return Result.success(orderRuleRepository.save(existing));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        orderRuleRepository.deleteById(id);
        return Result.success(null);
    }

    @Transactional
    @PutMapping("/{id}/apply")
    public Result<?> apply(@PathVariable Long id) {
        orderRuleRepository.setApplied(id, true);
        return Result.success(null);
    }

    @Transactional
    @PutMapping("/{id}/unapply")
    public Result<?> unapply(@PathVariable Long id) {
        orderRuleRepository.setApplied(id, false);
        return Result.success(null);
    }
}
