package com.borsibaar.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDto(
                @NotBlank String name,
                Boolean dynamicPricing) {
}
