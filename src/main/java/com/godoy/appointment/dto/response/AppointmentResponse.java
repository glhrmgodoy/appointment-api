package com.godoy.appointment.dto.response;

import com.godoy.appointment.domain.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        String clientName,
        String professionalName,
        String serviceName,
        BigDecimal servicePrice,
        Integer serviceDurationMinutes,
        LocalDateTime scheduledAt,
        AppointmentStatus status,
        String notes,
        LocalDateTime createdAt
) {
}
