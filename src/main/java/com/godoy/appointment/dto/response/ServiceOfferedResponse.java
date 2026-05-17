package com.godoy.appointment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOfferedResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer durationMinutes,
        String professionalName,
        LocalDateTime createdAt
) {
}
