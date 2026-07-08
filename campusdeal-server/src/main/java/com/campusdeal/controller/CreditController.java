package com.campusdeal.controller;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.entity.CreditRecord;
import com.campusdeal.entity.CreditRule;
import com.campusdeal.entity.UserCredit;
import com.campusdeal.service.CreditService;
import com.campusdeal.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credits")
public class CreditController {
    private final CreditService creditService;
    private final JwtUtil jwtUtil;

    public CreditController(CreditService creditService, JwtUtil jwtUtil) {
        this.creditService = creditService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/my")
    public Result<UserCredit> myCredit(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(creditService.getUserCredit(userId));
    }

    @GetMapping("/my/records")
    public Result<PageResult<CreditRecord>> myRecords(@RequestHeader("Authorization") String authHeader,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(creditService.getCreditRecords(userId, page, size));
    }

    @GetMapping("/rules")
    public Result<List<CreditRule>> rules() {
        return Result.success(creditService.getRules());
    }
}
