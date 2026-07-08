package com.campusdeal.controller.admin;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.entity.Order;
import com.campusdeal.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;
    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @GetMapping
    public Result<PageResult<Order>> list(@RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "8") int size) {
        return Result.success(orderService.getAdminOrders(status, page, size));
    }

    /** 订单统计 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(orderService.getOrderStats());
    }

    /** 催缴（支持自定义消息） */
    @PutMapping("/{id}/remind")
    public Result<?> remind(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String message = body != null ? body.get("message") : null;
        orderService.adminRemindPayment(id, message);
        return Result.success("已发送催缴通知", null);
    }

    /** 发货 */
    @PutMapping("/{id}/ship")
    public Result<?> ship(@PathVariable Long id) {
        orderService.adminShipOrder(id);
        return Result.success("发货成功", null);
    }

    /** 收货 */
    @PutMapping("/{id}/receive")
    public Result<?> receive(@PathVariable Long id) {
        orderService.adminReceiveOrder(id);
        return Result.success("收货成功", null);
    }

    /** 确认收货 (与 receive 等价，语义化别名) */
    @PutMapping("/{id}/confirm")
    public Result<?> confirm(@PathVariable Long id) {
        orderService.adminReceiveOrder(id);
        return Result.success("确认收货成功", null);
    }

    /** 删除订单（软删除，标记为 deleted 状态） */
    @PutMapping("/{id}/delete")
    public Result<?> delete(@PathVariable Long id) {
        orderService.adminDeleteOrder(id);
        return Result.success("已删除", null);
    }

    /** 获取订单详情（管理员） */
    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.success(orderService.getAdminOrderDetail(id));
    }
}
