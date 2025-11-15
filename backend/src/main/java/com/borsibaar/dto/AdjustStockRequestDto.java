package com.borsibaar.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AdjustStockRequestDto(
                @NotNull(message = "Product ID is required") Long productId,

                @NotNull(message = "New quantity is required") @DecimalMin(value = "0", message = "Quantity cannot be negative") BigDecimal newQuantity,

                String notes) {
}
