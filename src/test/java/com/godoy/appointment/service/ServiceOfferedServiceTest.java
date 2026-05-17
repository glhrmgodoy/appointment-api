package com.godoy.appointment.service;

import com.godoy.appointment.domain.entity.Professional;
import com.godoy.appointment.domain.entity.ServiceOffered;
import com.godoy.appointment.domain.enums.Specialty;
import com.godoy.appointment.dto.request.ServiceOfferedRequest;
import com.godoy.appointment.dto.response.ServiceOfferedResponse;
import com.godoy.appointment.exception.BusinessException;
import com.godoy.appointment.exception.NotFoundException;
import com.godoy.appointment.mapper.ServiceOfferedMapper;
import com.godoy.appointment.repository.ProfessionalRepository;
import com.godoy.appointment.repository.ServiceOfferedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOfferedServiceTest {

    @Mock
    private ServiceOfferedRepository serviceOfferedRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private ServiceOfferedMapper serviceOfferedMapper;

    @InjectMocks
    private ServiceOfferedService serviceOfferedService;

    private Professional professional;
    private ServiceOffered service;
    private ServiceOfferedRequest request;
    private ServiceOfferedResponse response;

    @BeforeEach
    void setUp() {
        professional = Professional.builder()
                .id(UUID.randomUUID())
                .name("Dra. Clara")
                .email("clara@email.com")
                .cpf("98765432100")
                .phone("11999999999")
                .specialty(Specialty.DOCTOR)
                .active(true)
                .build();

        service = ServiceOffered.builder()
                .id(UUID.randomUUID())
                .name("Consulta Clínica")
                .description("Consulta médica geral")
                .price(new BigDecimal("150.00"))
                .durationMinutes(30)
                .professional(professional)
                .active(true)
                .build();

        request = new ServiceOfferedRequest(
                "Consulta Clínica",
                "Consulta médica geral",
                new BigDecimal("150.00"),
                30,
                professional.getId()
        );

        response = new ServiceOfferedResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getPrice(),
                service.getDurationMinutes(),
                professional.getName(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar serviço com sucesso")
        void shouldCreateServiceSuccessfully() {
            when(professionalRepository.findById(professional.getId())).thenReturn(Optional.of(professional));
            when(serviceOfferedMapper.toEntity(request)).thenReturn(service);
            when(serviceOfferedRepository.save(any())).thenReturn(service);
            when(serviceOfferedMapper.toResponse(service)).thenReturn(response);

            ServiceOfferedResponse result = serviceOfferedService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Consulta Clínica");
            assertThat(result.price()).isEqualByComparingTo("150.00");
            assertThat(result.professionalName()).isEqualTo("Dra. Clara");
            verify(serviceOfferedRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando profissional não encontrado")
        void shouldThrowNotFoundExceptionWhenProfessionalNotFound() {
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceOfferedService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Profissional não encontrado");

            verify(serviceOfferedRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando profissional inativo")
        void shouldThrowBusinessExceptionWhenProfessionalInactive() {
            professional.setActive(false);
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.of(professional));

            assertThatThrownBy(() -> serviceOfferedService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Profissional inativo não pode ter serviços");

            verify(serviceOfferedRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retorna serviço por ID com sucesso")
        void shouldFindServiceByIdSuccessfully() {
            when(serviceOfferedRepository.findById(service.getId())).thenReturn(Optional.of(service));
            when(serviceOfferedMapper.toResponse(service)).thenReturn(response);

            ServiceOfferedResponse result = serviceOfferedService.findById(service.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(service.getId());
            assertThat(result.durationMinutes()).isEqualTo(30);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando serviço não encontrado")
        void shouldThrowNotFoundWhenServiceNotFound() {
            when(serviceOfferedRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceOfferedService.findById(service.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Serviço não encontrado");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista de serviços")
        void shouldReturnListOfServices() {
            when(serviceOfferedRepository.findAll()).thenReturn(List.of(service));
            when(serviceOfferedMapper.toResponse(service)).thenReturn(response);

            List<ServiceOfferedResponse> result = serviceOfferedService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Consulta Clínica");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há serviços")
        void shouldReturnEmptyListWhenNoService() {
            when(serviceOfferedRepository.findAll()).thenReturn(List.of());

            List<ServiceOfferedResponse> result = serviceOfferedService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByProfessionalId")
    class FindAllByProfessionalId {

        @Test
        @DisplayName("Deve retornar serviços do profissional")
        void shouldReturnServicesByProfessionalId() {
            when(professionalRepository.existsById(professional.getId())).thenReturn(true);
            when(serviceOfferedRepository.findAllByProfessionalId(professional.getId())).thenReturn(List.of(service));
            when(serviceOfferedMapper.toResponse(service)).thenReturn(response);

            List<ServiceOfferedResponse> result = serviceOfferedService.findAllByProfessionalId(professional.getId());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().professionalName()).isEqualTo("Dra. Clara");
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando profissional não encontrado")
        void shouldThrowNotFoundWhenProfessionalNotFound() {
            when(professionalRepository.existsById(any())).thenReturn(false);

            assertThatThrownBy(() -> serviceOfferedService.findAllByProfessionalId(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Profissional não encontrado");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar serviço com sucesso")
        void shouldUpdateServiceSuccessfully() {
            when(serviceOfferedRepository.findById(service.getId())).thenReturn(Optional.of(service));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.of(professional));
            when(serviceOfferedRepository.save(any())).thenReturn(service);
            when(serviceOfferedMapper.toResponse(service)).thenReturn(response);

            ServiceOfferedResponse result = serviceOfferedService.update(service.getId(), request);

            assertThat(result).isNotNull();
            verify(serviceOfferedRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFound ao atualizar serviço inexistente")
        void shouldThrowNotFoundWhenUpdatingNonExistentService() {
            when(serviceOfferedRepository.findById(service.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceOfferedService.update(service.getId(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Serviço não encontrado");

            verify(serviceOfferedRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFound ao atualizar com profissional inexistente")
        void shouldThrowNotFoundWhenProfessionalNotFoundOnUpdate() {
            when(serviceOfferedRepository.findById(service.getId())).thenReturn(Optional.of(service));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceOfferedService.update(service.getId(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Profissional não encontrado");

            verify(serviceOfferedRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar com profissional inativo")
        void shouldThrowBusinessExceptionWhenProfessionalInactiveOnUpdate() {
            professional.setActive(false);
            when(serviceOfferedRepository.findById(service.getId())).thenReturn(Optional.of(service));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.of(professional));

            assertThatThrownBy(() -> serviceOfferedService.update(service.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Profissional inativo não pode ter serviços");

            verify(serviceOfferedRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve inativar serviço com sucesso")
        void shouldDeleteServiceSuccessfully() {
            when(serviceOfferedRepository.findById(service.getId())).thenReturn(Optional.of(service));

            serviceOfferedService.delete(service.getId());

            verify(serviceOfferedRepository).save(argThat(s -> !s.getActive()));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao inativar serivço inexistente")
        void shouldThrowNotFoundWhenDeletingNonExistentService() {
            when(serviceOfferedRepository.findById(service.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> serviceOfferedService.delete(service.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Serviço não encontrado");

            verify(serviceOfferedRepository, never()).save(any());
        }
    }

}