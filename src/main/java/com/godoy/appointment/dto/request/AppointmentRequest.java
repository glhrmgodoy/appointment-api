package com.godoy.appointment.dto.request;

import com.godoy.appointment.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentRequest(

        @NotNull(message = "ID do cliente é obrigatório")
        UUID clientId,

        @NotNull(message = "ID do profissional é obrigatório")
        UUID professionalId,

        @NotNull(message = "ID do serviço é obrigatório")
        UUID serviceId,

        @NotNull(message = "Data e hora são obrigatórios")
        @Future(message = "Data e hora devem ser futuras")
        LocalDateTime scheduledAt,

        String notes
) {
}
