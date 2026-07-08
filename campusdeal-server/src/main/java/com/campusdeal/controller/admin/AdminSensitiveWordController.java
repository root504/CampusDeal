package com.campusdeal.controller.admin;

import com.campusdeal.common.Result;
import com.campusdeal.entity.SensitiveWord;
import com.campusdeal.service.SensitiveWordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/sensitive-words")
public class AdminSensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public AdminSensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    /** 获取全部敏感词 */
    @GetMapping
    public Result<List<SensitiveWord>> list() {
        return Result.success(sensitiveWordService.getAllWords());
    }

    /** 创建敏感词 */
    @PostMapping
    public Result<SensitiveWord> create(@RequestBody SensitiveWord word) {
        return Result.success(sensitiveWordService.createWord(word));
    }

    /** 更新敏感词 */
    @PutMapping("/{id}")
    public Result<SensitiveWord> update(@PathVariable Long id, @RequestBody SensitiveWord word) {
        return Result.success(sensitiveWordService.updateWord(id, word));
    }

    /** 删除敏感词 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        sensitiveWordService.deleteWord(id);
        return Result.success(null);
    }

    /** 应用敏感词 */
    @PutMapping("/{id}/apply")
    public Result<?> apply(@PathVariable Long id) {
        sensitiveWordService.applyWord(id);
        return Result.success(null);
    }

    /** 取消应用 */
    @PutMapping("/{id}/unapply")
    public Result<?> unapply(@PathVariable Long id) {
        sensitiveWordService.unapplyWord(id);
        return Result.success(null);
    }
}
