package com.borsibaar.dto;

import java.time.OffsetDateTime;

public record OrganizationResponseDto(
                Long id,
                String name,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt) {
}
