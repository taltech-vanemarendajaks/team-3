package com.borsibaar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record BarStationRequestDto(
    @NotBlank(message = "Station name is required")
    @Size(max = 120, message = "Station name must not exceed 120 characters")
    String name,

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    Boolean isActive,
    List<UUID> userIds
) {
}

