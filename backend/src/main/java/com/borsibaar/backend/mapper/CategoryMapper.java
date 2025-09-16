package com.borsibaar.backend.mapper;

import com.borsibaar.backend.dto.CategoryRequest;
import com.borsibaar.backend.dto.CategoryResponse;
import com.borsibaar.backend.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CategoryRequest request);
    CategoryResponse toResponse(Category category);
}
