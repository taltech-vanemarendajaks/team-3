package com.borsibaar.mapper;

import com.borsibaar.dto.CategoryRequestDto;
import com.borsibaar.dto.CategoryResponseDto;
import com.borsibaar.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    Category toEntity(CategoryRequestDto request);

    CategoryResponseDto toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    void updateEntity(@MappingTarget Category category, CategoryRequestDto request);
}
