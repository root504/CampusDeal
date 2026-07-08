package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.common.PageResult;
import com.campusdeal.entity.Favorite;
import com.campusdeal.entity.Product;
import com.campusdeal.repository.FavoriteRepository;
import com.campusdeal.repository.ProductRepository;
import com.campusdeal.service.FavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    public FavoriteServiceImpl(FavoriteRepository favoriteRepository, ProductRepository productRepository) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
    }


    @Override
    @Transactional(readOnly = true)
    public PageResult<Favorite> getFavorites(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Favorite> favPage = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        // 触发懒加载的 product 关联，避免序列化时 product 为 null
        for (Favorite f : favPage.getContent()) {
            if (f.getProduct() != null) {
                f.getProduct().getTitle();
                if (f.getProduct().getSeller() != null) f.getProduct().getSeller().getUsername();
                if (f.getProduct().getCategory() != null) f.getProduct().getCategory().getName();
            }
        }
        return PageResult.of(favPage.getContent(), favPage.getTotalElements(), page, size);
    }

    @Override
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new BusinessException("商品不存在");
        }

        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BusinessException("已收藏该商品");
        }

        Favorite favorite = Favorite.builder()
                .userId(userId)
                .productId(productId)
                .build();
        favoriteRepository.save(favorite);

        // 更新商品收藏数
        productRepository.findById(productId).ifPresent(p -> {
            p.setFavoriteCount(p.getFavoriteCount() + 1);
            productRepository.save(p);
        });
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        if (!favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BusinessException("未收藏该商品");
        }

        favoriteRepository.deleteByUserIdAndProductId(userId, productId);

        productRepository.findById(productId).ifPresent(p -> {
            p.setFavoriteCount(Math.max(0, p.getFavoriteCount() - 1));
            productRepository.save(p);
        });
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }
}
