package com.godoy.appointment.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,
        String email,
        String cpf,
        String phone,
        LocalDateTime createdAt
) {
}
