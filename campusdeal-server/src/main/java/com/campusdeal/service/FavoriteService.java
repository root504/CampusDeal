package com.campusdeal.service;

import com.campusdeal.common.PageResult;
import com.campusdeal.entity.Favorite;

public interface FavoriteService {
    PageResult<Favorite> getFavorites(Long userId, int page, int size);
    void addFavorite(Long userId, Long productId);
    void removeFavorite(Long userId, Long productId);
    boolean isFavorited(Long userId, Long productId);
}
