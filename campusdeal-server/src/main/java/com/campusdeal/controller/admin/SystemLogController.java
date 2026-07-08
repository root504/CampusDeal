package com.campusdeal.controller.admin;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.entity.SystemLog;
import com.campusdeal.service.SystemLogService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/logs")
public class SystemLogController {
    private final SystemLogService systemLogService;

    public SystemLogController(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    @GetMapping
    public Result<PageResult<SystemLog>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.success(systemLogService.getLogs(page, size));
    }

    @DeleteMapping
    public Result<?> deleteAll() {
        systemLogService.deleteAllLogs();
        return Result.success(null);
    }
}
