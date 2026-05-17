package com.godoy.appointment.service;

import com.godoy.appointment.domain.entity.Professional;
import com.godoy.appointment.domain.enums.Specialty;
import com.godoy.appointment.dto.request.ProfessionalRequest;
import com.godoy.appointment.dto.response.ProfessionalResponse;
import com.godoy.appointment.exception.BusinessException;
import com.godoy.appointment.exception.NotFoundException;
import com.godoy.appointment.mapper.ProfessionalMapper;
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


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfessionalServiceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private ServiceOfferedRepository serviceOfferedRepository;

    @Mock
    private ProfessionalMapper professionalMapper;

    @InjectMocks
    private ProfessionalService professionalService;

    private Professional professional;
    private ProfessionalRequest request;
    private ProfessionalResponse response;

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

        request = new ProfessionalRequest(
                "Dra. Clara",
                "clara@email.com",
                "98765432100",
                "11888888888",
                Specialty.DOCTOR
        );

        response = new ProfessionalResponse(
                professional.getId(),
                professional.getName(),
                professional.getEmail(),
                professional.getCpf(),
                professional.getPhone(),
                professional.getSpecialty(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar profissional com sucesso")
        void shouldCreateProfessionalSuccessfully() {
            when(professionalRepository.existsByEmail(request.email())).thenReturn(false);
            when(professionalRepository.existsByCpf(request.cpf())).thenReturn(false);
            when(professionalMapper.toEntity(request)).thenReturn(professional);
            when(professionalRepository.save(any())).thenReturn(professional);
            when(professionalMapper.toResponse(professional)).thenReturn(response);

            ProfessionalResponse result = professionalService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Dra. Clara");
            assertThat(result.specialty()).isEqualTo(Specialty.DOCTOR);
            verify(professionalRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando email já cadastrado")
        void shouldThrowBusinessExceptionWhenEmailAlreadyExists() {
            when(professionalRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> professionalService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email já cadastrado");

            verify(professionalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando CPF já cadastrado")
        void shouldThrowBusinessExceptionWhenCPFAlreadyExists() {
            when(professionalRepository.existsByCpf(request.cpf())).thenReturn(true);

            assertThatThrownBy(() -> professionalService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("CPF já cadastrado");

            verify(professionalRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar profissional por ID com sucesso")
        void shouldFindProfessionalByIdSuccessfully() {
            when(professionalRepository.findById(professional.getId())).thenReturn(Optional.of(professional));
            when(professionalMapper.toResponse(professional)).thenReturn(response);

            ProfessionalResponse result = professionalService.findById(professional.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(professional.getId());
            assertThat(result.specialty()).isEqualTo(Specialty.DOCTOR);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando profissional não encontrado")
        void shouldThrowNotFoundWhenProfessionalNotFound() {
            when(professionalRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> professionalService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Profissional não encontrado");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista de profissionais")
        void shouldReturnListOfProfessionals() {
            when(professionalRepository.findAll()).thenReturn(List.of(professional));
            when(professionalMapper.toResponse(professional)).thenReturn(response);

            List<ProfessionalResponse> result = professionalService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Dra. Clara");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há profissionais")
        void shouldReturnEmptyListWhenNoProfessionals() {
            when(professionalRepository.findAll()).thenReturn(List.of());

            List<ProfessionalResponse> result = professionalService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar profissional com sucesso")
        void shouldUpdateProfessionalSuccessfully() {
            professional.setEmail("email@email.com");
            professional.setCpf("99999999999");
            when(professionalRepository.findById(professional.getId())).thenReturn(Optional.of(professional));
            when(professionalRepository.existsByEmail(request.email())).thenReturn(false);
            when(professionalRepository.existsByCpf(request.cpf())).thenReturn(false);
            when(professionalRepository.save(any())).thenReturn(professional);
            when(professionalMapper.toResponse(professional)).thenReturn(response);

            ProfessionalResponse result = professionalService.update(professional.getId(), request);

            assertThat(result).isNotNull();
            verify(professionalRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar profissional inexistente")
        void shouldThrowNotFoundWhenUpdatingNonExistentProfessional() {
            when(professionalRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> professionalService.update(UUID.randomUUID(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Profissional não encontrado");

            verify(professionalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar com email já usado por outro profissional")
        void shouldThrowBusinessExceptionWhenEmailUsedByAnotherProfessional() {
            professional.setEmail("email@email.com");
            when(professionalRepository.findById(professional.getId())).thenReturn(Optional.of(professional));
            when(professionalRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> professionalService.update(professional.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email já cadastrado");

            verify(professionalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar com CPF já usado por outro profissional")
        void shouldThrowBusinessExceptionWhenCpfUsedByAnotherProfessional() {
            professional.setCpf("99999999999");
            when(professionalRepository.findById(professional.getId())).thenReturn(Optional.of(professional));
            when(professionalRepository.existsByCpf(request.cpf())).thenReturn(true);

            assertThatThrownBy(() -> professionalService.update(professional.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("CPF já cadastrado");

            verify(professionalRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve inativar profissional com sucesso")
        void shouldDeleteProfessionalSuccessfully() {
            when(professionalRepository.findById(professional.getId())).thenReturn(Optional.of(professional));
            when(serviceOfferedRepository.existsByProfessionalIdAndActiveTrue(professional.getId())).thenReturn(false);

            professionalService.delete(professional.getId());

            verify(professionalRepository).save(argThat(p -> !p.getActive()));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao inativar profissional inexistente")
        void shouldThrowNotFoundWhenDeletingNonExistentProfessional() {
            when(professionalRepository.findById(professional.getId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> professionalService.delete(professional.getId()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Profissional não encontrado");

            verify(professionalRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao inativar profissional com serviços ativos")
        void shouldThrowBusinessExceptionWhenProfessionalActiveService() {
            when(professionalRepository.findById(professional.getId())).thenReturn(Optional.of(professional));
            when(serviceOfferedRepository.existsByProfessionalIdAndActiveTrue(professional.getId())).thenReturn(true);

            assertThatThrownBy(() -> professionalService.delete(professional.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Profissional possui serviços ativos e não pode ser inativado");

            verify(professionalRepository, never()).save(any());
        }
    }

}