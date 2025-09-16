package com.borsibaar.backend.controller;

import com.borsibaar.backend.dto.CategoryRequest;
import com.borsibaar.backend.dto.CategoryResponse;
import com.borsibaar.backend.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@RequestBody CategoryRequest request) {
        return categoryService.categoryResponse(request);
    }

}
