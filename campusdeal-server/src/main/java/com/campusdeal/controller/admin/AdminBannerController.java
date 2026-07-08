package com.campusdeal.controller.admin;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.entity.Banner;
import com.campusdeal.repository.BannerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/banners")
public class AdminBannerController {

    private final BannerRepository bannerRepository;

    public AdminBannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    /**
     * 管理列表（分页，按 sort_order 升序）
     */
    @GetMapping
    public Result<PageResult<Banner>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        Page<Banner> bannerPage = bannerRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "sortOrder"))
        );
        PageResult<Banner> pr = PageResult.of(
                bannerPage.getContent(), bannerPage.getTotalElements(), page, size);
        return Result.success(pr);
    }

    /**
     * 新增轮播图
     */
    @PostMapping
    public Result<Banner> create(@RequestBody Map<String, String> body) {
        Banner banner = new Banner();
        banner.setImageUrl(body.get("imageUrl"));
        if (body.containsKey("linkUrl")) {
            banner.setLinkUrl(body.get("linkUrl"));
        }
        // 新 Banner 默认排在最后
        List<Banner> all = bannerRepository.findAllByOrderBySortOrderAsc();
        banner.setSortOrder(all.isEmpty() ? 1 : all.get(all.size() - 1).getSortOrder() + 1);
        banner.setStatus(1);
        Banner saved = bannerRepository.save(banner);
        return Result.success(saved);
    }

    /**
     * 更新轮播图
     */
    @PutMapping("/{id}")
    public Result<Banner> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("轮播图不存在"));
        if (body.containsKey("imageUrl")) {
            banner.setImageUrl(body.get("imageUrl"));
        }
        if (body.containsKey("linkUrl")) {
            banner.setLinkUrl(body.get("linkUrl"));
        }
        Banner saved = bannerRepository.save(banner);
        return Result.success(saved);
    }

    /**
     * 调整顺序（上下移动）
     */
    @PutMapping("/{id}/order")
    public Result<?> adjustOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String direction = body.get("direction");
        Banner current = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("轮播图不存在"));

        List<Banner> all = bannerRepository.findAllByOrderBySortOrderAsc();
        int idx = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(id)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) return Result.error("未找到该轮播图");

        if ("up".equals(direction) && idx > 0) {
            Banner prev = all.get(idx - 1);
            int tmp = current.getSortOrder();
            current.setSortOrder(prev.getSortOrder());
            prev.setSortOrder(tmp);
            bannerRepository.save(current);
            bannerRepository.save(prev);
        } else if ("down".equals(direction) && idx < all.size() - 1) {
            Banner next = all.get(idx + 1);
            int tmp = current.getSortOrder();
            current.setSortOrder(next.getSortOrder());
            next.setSortOrder(tmp);
            bannerRepository.save(current);
            bannerRepository.save(next);
        }
        return Result.success(null);
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        bannerRepository.deleteById(id);
        return Result.success(null);
    }

    /**
     * 应用排序：将所有 status=1 的轮播图按当前 sort_order 重新编号为 1,2,3...
     */
    @PostMapping("/apply")
    public Result<?> apply() {
        List<Banner> active = bannerRepository.findByStatusOrderBySortOrderAsc(1);
        for (int i = 0; i < active.size(); i++) {
            active.get(i).setSortOrder(i + 1);
        }
        bannerRepository.saveAll(active);
        return Result.success("应用成功");
    }
}
