package com.borsibaar.backend.dto;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String description,
        BigDecimal currentPrice,
        Long categoryId
) {}
