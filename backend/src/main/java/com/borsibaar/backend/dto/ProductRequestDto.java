package com.borsibaar.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequestDto(
        @NotBlank(message = "Product name is required")
        @Size(max = 120, message = "Product name must not exceed 120 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Current price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal currentPrice,

        @NotNull(message = "Category ID is required")
        Long categoryId
) {}
