package com.borsibaar.dto;

import jakarta.validation.constraints.NotBlank;

public record OrganizationRequestDto(
                @NotBlank String name) {
}
