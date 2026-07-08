package com.campusdeal.controller;

import com.campusdeal.common.Result;
import com.campusdeal.entity.Category;
import com.campusdeal.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public Result<List<Category>> listCategories() {
        List<Category> categories = categoryRepository.findAll(
                Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("id"))
        );
        return Result.success(categories);
    }
}
