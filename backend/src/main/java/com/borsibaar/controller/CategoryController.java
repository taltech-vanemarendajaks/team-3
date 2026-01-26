package com.borsibaar.controller;

import com.borsibaar.dto.CategoryRequestDto;
import com.borsibaar.dto.CategoryResponseDto;
import com.borsibaar.service.CategoryService;
import com.borsibaar.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto createCategory(@RequestBody CategoryRequestDto request) {
        return categoryService.create(request, SecurityUtils.getCurrentOrganizationId());
    }

    @GetMapping
    public List<CategoryResponseDto> getAll(@RequestParam(required = false) Long organizationId) {
        // If organizationId is provided, use it (for public access)
        // Otherwise, get from authenticated user
        Long orgId = organizationId != null ? organizationId : SecurityUtils.getCurrentOrganizationId();
        return categoryService.getAllByOrg(orgId);
    }

    @GetMapping("/{id}")
    public CategoryResponseDto getById(@PathVariable Long id) {
        return categoryService.getByIdAndOrg(id, SecurityUtils.getCurrentOrganizationId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.deleteReturningDto(id, SecurityUtils.getCurrentOrganizationId());
    }
}
