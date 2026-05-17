package com.godoy.appointment.mapper;

import com.godoy.appointment.domain.entity.ServiceOffered;
import com.godoy.appointment.dto.request.ServiceOfferedRequest;
import com.godoy.appointment.dto.response.ServiceOfferedResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceOfferedMapper {

    @Mapping(target = "professional", ignore = true)
    ServiceOffered toEntity(ServiceOfferedRequest request);

    @Mapping(source = "professional.name", target = "professionalName")
    ServiceOfferedResponse toResponse(ServiceOffered service);
}
