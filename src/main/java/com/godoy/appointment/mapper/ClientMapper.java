package com.godoy.appointment.mapper;

import com.godoy.appointment.domain.entity.Client;
import com.godoy.appointment.dto.request.ClientRequest;
import com.godoy.appointment.dto.response.ClientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    Client toEntity(ClientRequest request);

    ClientResponse toResponse(Client client);
}
