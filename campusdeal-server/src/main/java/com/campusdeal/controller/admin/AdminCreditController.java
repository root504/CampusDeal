package com.campusdeal.controller.admin;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.entity.CreditRecord;
import com.campusdeal.entity.CreditRule;
import com.campusdeal.service.CreditService;
import com.campusdeal.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/credits")
public class AdminCreditController {
    private final CreditService creditService;
    private final JwtUtil jwtUtil;

    public AdminCreditController(CreditService creditService, JwtUtil jwtUtil) {
        this.creditService = creditService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return Result.success(creditService.getAllUserCreditsWithName(page, size));
    }

    @GetMapping("/{userId}/records")
    public Result<PageResult<CreditRecord>> userRecords(@PathVariable Long userId,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return Result.success(creditService.getCreditRecords(userId, page, size));
    }

    @PostMapping("/change")
    public Result<?> changeCredit(@RequestHeader("Authorization") String authHeader,
                                   @RequestBody Map<String, Object> body) {
        Long operatorId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        Long userId = Long.valueOf(body.get("userId").toString());
        Integer amount = Integer.valueOf(body.get("amount").toString());
        String reason = (String) body.get("reason");
        creditService.changeCredit(userId, amount, reason, operatorId);
        return Result.success(null);
    }

    @GetMapping("/rules")
    public Result<List<CreditRule>> rules() {
        return Result.success(creditService.getRules());
    }

    @PostMapping("/rules")
    public Result<CreditRule> createRule(@RequestBody CreditRule rule) {
        return Result.success(creditService.createRule(rule));
    }

    @PutMapping("/rules/{id}")
    public Result<CreditRule> updateRule(@PathVariable Long id, @RequestBody CreditRule rule) {
        return Result.success(creditService.updateRule(id, rule));
    }

    @DeleteMapping("/rules/{id}")
    public Result<?> deleteRule(@PathVariable Long id) {
        creditService.deleteRule(id);
        return Result.success(null);
    }

    @PutMapping("/rules/{id}/apply")
    public Result<?> applyRule(@PathVariable Long id) {
        creditService.applyRule(id);
        return Result.success(null);
    }

    @PutMapping("/rules/{id}/unapply")
    public Result<?> unapplyRule(@PathVariable Long id) {
        creditService.unapplyRule(id);
        return Result.success(null);
    }
}
