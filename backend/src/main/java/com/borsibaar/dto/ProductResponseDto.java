package com.borsibaar.dto;

import java.math.BigDecimal;

public record ProductResponseDto(
                Long id,
                String name,
                String description,
                BigDecimal currentPrice,
                BigDecimal minPrice,
                BigDecimal maxPrice,
                Long categoryId,
                String categoryName) {
}
