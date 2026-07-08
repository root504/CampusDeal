package com.campusdeal.controller;

import com.campusdeal.common.PageResult;
import com.campusdeal.common.Result;
import com.campusdeal.entity.Favorite;
import com.campusdeal.service.FavoriteService;
import com.campusdeal.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;
    private final JwtUtil jwtUtil;
    public FavoriteController(FavoriteService favoriteService, JwtUtil jwtUtil) {
        this.favoriteService = favoriteService;
        this.jwtUtil = jwtUtil;
    }


    @GetMapping
    public Result<PageResult<Favorite>> list(@RequestHeader("Authorization") String authHeader,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(favoriteService.getFavorites(userId, page, size));
    }

    @PostMapping
    public Result<?> add(@RequestHeader("Authorization") String authHeader,
                          @RequestBody Map<String, Long> body) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        favoriteService.addFavorite(userId, body.get("productId"));
        return Result.success(null);
    }

    @DeleteMapping("/{productId}")
    public Result<?> remove(@RequestHeader("Authorization") String authHeader,
                             @PathVariable Long productId) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        favoriteService.removeFavorite(userId, productId);
        return Result.success(null);
    }

    @GetMapping("/check/{productId}")
    public Result<Boolean> check(@RequestHeader("Authorization") String authHeader,
                                  @PathVariable Long productId) {
        Long userId = jwtUtil.getUserIdFromToken(authHeader.replace("Bearer ", ""));
        return Result.success(favoriteService.isFavorited(userId, productId));
    }
}
