package com.borsibaar.mapper;

import com.borsibaar.dto.ProductRequestDto;
import com.borsibaar.dto.ProductResponseDto;
import com.borsibaar.entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "currentPrice", source = "basePrice")
    ProductResponseDto toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationId", ignore = true) // set in service
    @Mapping(target = "basePrice", source = "currentPrice")
    @Mapping(target = "minPrice", source = "minPrice")
    @Mapping(target = "maxPrice", source = "maxPrice")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductRequestDto request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "basePrice", source = "currentPrice")
    @Mapping(target = "minPrice", source = "minPrice")
    @Mapping(target = "maxPrice", source = "maxPrice")
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Product product, ProductRequestDto request);
}
