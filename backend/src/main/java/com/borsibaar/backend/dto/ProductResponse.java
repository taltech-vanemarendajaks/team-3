package com.borsibaar.backend.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal currentPrice,
        Long categoryId,
        String categoryName
) {}
