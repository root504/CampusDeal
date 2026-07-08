package com.campusdeal.controller;

import com.campusdeal.common.Result;
import com.campusdeal.entity.Banner;
import com.campusdeal.repository.BannerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banners")
public class BannerController {

    private final BannerRepository bannerRepository;

    public BannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    /**
     * 公开接口：获取所有上架轮播图（status=1），按 sort_order 升序
     */
    @GetMapping
    public Result<List<Banner>> listPublished() {
        List<Banner> list = bannerRepository.findByStatusOrderBySortOrderAsc(1);
        return Result.success(list);
    }
}
