package com.godoy.appointment.mapper;

import com.godoy.appointment.domain.entity.Appointment;
import com.godoy.appointment.dto.request.AppointmentRequest;
import com.godoy.appointment.dto.response.AppointmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AppointmentMapper {

    @Mapping(target = "client", ignore = true)
    @Mapping(target = "professional", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "status", ignore = true)
    Appointment toEntity(AppointmentRequest request);

    @Mapping(source = "client.name", target = "clientName")
    @Mapping(source = "professional.name", target = "professionalName")
    @Mapping(source = "service.name", target = "serviceName")
    @Mapping(source = "service.price", target = "servicePrice")
    @Mapping(source = "service.durationMinutes", target = "serviceDurationMinutes")
    AppointmentResponse toResponse(Appointment appointment);
}
