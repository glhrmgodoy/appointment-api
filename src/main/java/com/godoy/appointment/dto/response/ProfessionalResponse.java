package com.godoy.appointment.dto.response;

import com.godoy.appointment.domain.enums.Specialty;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProfessionalResponse(
        UUID id,
        String name,
        String email,
        String cpf,
        String phone,
        Specialty specialty,
        LocalDateTime createdAt
) {
}
