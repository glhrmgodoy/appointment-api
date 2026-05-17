package com.godoy.appointment.mapper;

import com.godoy.appointment.domain.entity.Professional;
import com.godoy.appointment.dto.request.ProfessionalRequest;
import com.godoy.appointment.dto.response.ProfessionalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfessionalMapper {

    Professional toEntity(ProfessionalRequest request);

    ProfessionalResponse toResponse(Professional professional);
}
