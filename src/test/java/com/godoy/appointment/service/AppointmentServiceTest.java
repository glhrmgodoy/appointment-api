package com.godoy.appointment.service;

import com.godoy.appointment.domain.entity.Appointment;
import com.godoy.appointment.domain.entity.Client;
import com.godoy.appointment.domain.entity.Professional;
import com.godoy.appointment.domain.entity.ServiceOffered;
import com.godoy.appointment.domain.enums.AppointmentStatus;
import com.godoy.appointment.domain.enums.Specialty;
import com.godoy.appointment.dto.request.AppointmentRequest;
import com.godoy.appointment.dto.response.AppointmentResponse;
import com.godoy.appointment.exception.BusinessException;
import com.godoy.appointment.exception.NotFoundException;
import com.godoy.appointment.mapper.AppointmentMapper;
import com.godoy.appointment.repository.AppointmentRepository;
import com.godoy.appointment.repository.ClientRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private ServiceOfferedRepository serviceOfferedRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentService appointmentService;

    private Client client;
    private Professional professional;
    private ServiceOffered service;
    private Appointment appointment;
    private AppointmentRequest request;
    private AppointmentResponse response;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(UUID.randomUUID())
                .name("Maria Clara")
                .email("mariaclara@email.com")
                .cpf("12345678910")
                .phone("11999999999")
                .active(true)
                .build();

        professional = Professional.builder()
                .id(UUID.randomUUID())
                .name("Dr. Hélio")
                .email("helio@email.com")
                .cpf("98765432100")
                .phone("11888888888")
                .specialty(Specialty.DOCTOR)
                .active(true)
                .build();

        service = ServiceOffered.builder()
                .id(UUID.randomUUID())
                .name("Consulta Clínica")
                .price(new BigDecimal("150.00"))
                .durationMinutes(30)
                .professional(professional)
                .active(true)
                .build();

        request = new AppointmentRequest(
                client.getId(),
                professional.getId(),
                service.getId(),
                LocalDateTime.now().plusDays(1),
                "Primeira consulta"
        );

        appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .client(client)
                .professional(professional)
                .service(service)
                .scheduledAt(request.scheduledAt())
                .status(AppointmentStatus.PENDING)
                .notes(request.notes())
                .build();

        response = new AppointmentResponse(
                appointment.getId(),
                client.getName(),
                professional.getName(),
                service.getName(),
                service.getPrice(),
                service.getDurationMinutes(),
                appointment.getScheduledAt(),
                appointment.getStatus(),
                appointment.getNotes(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar agendamento com sucesso")
        void shouldCreateAppointmentSuccessfully() {
            when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.of(professional));
            when(serviceOfferedRepository.findById(request.serviceId())).thenReturn(Optional.of(service));
            when(appointmentRepository.existsByProfessionalIdAndScheduledAt(any(), any())).thenReturn(false);
            when(appointmentRepository.existsByClientIdAndScheduledAt(any(), any())).thenReturn(false);
            when(appointmentMapper.toEntity(request)).thenReturn(appointment);
            when(appointmentRepository.save(any())).thenReturn(appointment);
            when(appointmentMapper.toResponse(appointment)).thenReturn(response);

            AppointmentResponse result = appointmentService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(AppointmentStatus.PENDING);
            assertThat(result.clientName()).isEqualTo("Maria Clara");
            assertThat(result.professionalName()).isEqualTo("Dr. Hélio");
            verify(appointmentRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando cliente não encontrado")
        void shouldThrowNotFoundWhenClientNotFound() {
            when(clientRepository.findById(request.clientId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cliente não encontrado");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando cliente está inativo")
        void shouldThrowBusinessExceptionWhenClientInactive() {
            client.setActive(false);
            when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));

            assertThatThrownBy(() -> appointmentService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Cliente inativo não pode realizar agendamentos");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando profissional não encontrado")
        void shouldThrowNotFoundWhenProfessionalNotFound() {
            when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Profissional não encontrado");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando profissional está inativo")
        void shouldThrowBusinessExceptionWhenProfessionalInactive() {
            professional.setActive(false);
            when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.of(professional));

            assertThatThrownBy(() -> appointmentService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Profissional inativo não pode receber agendamentos");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando serviço está inativo")
        void shouldThrowBusinessExceptionWhenServiceInactive() {
            service.setActive(false);
            when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.of(professional));
            when(serviceOfferedRepository.findById(request.serviceId())).thenReturn(Optional.of(service));

            assertThatThrownBy(() -> appointmentService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Serviço inativo não pode ser agendado");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando serviço não encontrado")
        void shouldThrowNotFoundWhenServiceNotFound() {
            when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.of(professional));
            when(serviceOfferedRepository.findById(request.serviceId())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Serviço não encontrado");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando serviço não pertence ao profissional")
        void shouldThrowBusinessExceptionWhenServiceDoesNotBelongToProfessional() {
            Professional otherProfessional = Professional.builder()
                    .id(UUID.randomUUID())
                    .active(true)
                    .build();
            service.setProfessional(otherProfessional);

            when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.of(professional));
            when(serviceOfferedRepository.findById(request.serviceId())).thenReturn(Optional.of(service));

            assertThatThrownBy(() -> appointmentService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Serviço não pertence ao profissional informado");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando profissional já tem agendamento no horário")
        void shouldThrowBusinessExceptionWhenProfessionalHasConflict() {
            when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.of(professional));
            when(serviceOfferedRepository.findById(request.serviceId())).thenReturn(Optional.of(service));
            when(appointmentRepository.existsByProfessionalIdAndScheduledAt(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> appointmentService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Profissional já possui agendamento neste horário");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando cliente já tem agendamento no horário")
        void shouldThrowBusinessExceptionWhenClientHasConflict() {
            when(clientRepository.findById(request.clientId())).thenReturn(Optional.of(client));
            when(professionalRepository.findById(request.professionalId())).thenReturn(Optional.of(professional));
            when(serviceOfferedRepository.findById(request.serviceId())).thenReturn(Optional.of(service));
            when(appointmentRepository.existsByProfessionalIdAndScheduledAt(any(), any())).thenReturn(false);
            when(appointmentRepository.existsByClientIdAndScheduledAt(any(), any())).thenReturn(true);

            assertThatThrownBy(() -> appointmentService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Cliente já possui agendamento neste horário");

            verify(appointmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("confirm")
    class Confirm {

        @Test
        @DisplayName("Deve confirmar agendamento com sucesso")
        void shouldConfirmAppointmentSuccessfully() {
            when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any())).thenReturn(appointment);
            when(appointmentMapper.toResponse(any())).thenReturn(response);

            AppointmentResponse result = appointmentService.confirm(appointment.getId());

            assertThat(result).isNotNull();
            verify(appointmentRepository).save(argThat(a ->
                    a.getStatus().equals(AppointmentStatus.CONFIRMED)
            ));
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao confirmar agendamento não PENDING")
        void shouldThrowBusinessExceptionWhenConfirmingNonPendingAppointment() {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

            assertThatThrownBy(() -> appointmentService.confirm(appointment.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Apenas agendamentos PENDING podem ser confirmados");

            verify(appointmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("complete")
    class Complete {

        @Test
        @DisplayName("Deve concluir agendamento com sucesso")
        void shouldCompleteAppointmentSuccessfully() {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any())).thenReturn(appointment);
            when(appointmentMapper.toResponse(any())).thenReturn(response);

            AppointmentResponse result = appointmentService.complete(appointment.getId());

            assertThat(result).isNotNull();
            verify(appointmentRepository).save(argThat(a ->
                    a.getStatus().equals(AppointmentStatus.COMPLETED)
            ));
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao concluir agendamento não CONFIRMED")
        void shouldThrowBusinessExceptionWhenCompletingNonConfirmedAppointment() {
            when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

            assertThatThrownBy(() -> appointmentService.complete(appointment.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Apenas agendamentos CONFIRMED podem ser concluídos");

            verify(appointmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("Deve cancelar agendamento com sucesso")
        void shouldCancelAppointmentSuccessfully() {
            appointment.setScheduledAt(LocalDateTime.now().plusDays(1));
            when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any())).thenReturn(appointment);

            appointmentService.cancel(appointment.getId());

            verify(appointmentRepository).save(argThat(a ->
                    a.getStatus().equals(AppointmentStatus.CANCELLED)
            ));
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao cancelar agendamento COMPLETED")
        void shouldThrowBusinessExceptionWhenCancellingCompletedAppointment() {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

            assertThatThrownBy(() -> appointmentService.cancel(appointment.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Apenas agendamentos PENDING ou CONFIRMED podem ser cancelados");

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao cancelar com menos de 2h de antecedência")
        void shouldThrowBusinessExceptionWhenCancellingWithLessThan2Hours() {
            appointment.setScheduledAt(LocalDateTime.now().plusMinutes(30));
            when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));

            assertThatThrownBy(() -> appointmentService.cancel(appointment.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Cancelamento com menos de 2h de antecedência não é permitido");

            verify(appointmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar agendamento por ID com sucesso")
        void shouldFindAppointmentByIdSuccessfully() {
            when(appointmentRepository.findById(appointment.getId())).thenReturn(Optional.of(appointment));
            when(appointmentMapper.toResponse(appointment)).thenReturn(response);

            AppointmentResponse result = appointmentService.findById(appointment.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(appointment.getId());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando agendamento não encontrado")
        void shouldThrowNotFoundWhenAppointmentNotFound() {
            when(appointmentRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Agendamento não encontrado");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista de agendamentos")
        void shouldReturnListOfAppointments() {
            when(appointmentRepository.findAll()).thenReturn(List.of(appointment));
            when(appointmentMapper.toResponse(appointment)).thenReturn(response);

            List<AppointmentResponse> result = appointmentService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().clientName()).isEqualTo("Maria Clara");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há agendamentos")
        void shouldReturnEmptyListWhenNoAppointments() {
            when(appointmentRepository.findAll()).thenReturn(List.of());

            List<AppointmentResponse> result = appointmentService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByClientId")
    class FindAllByClientId {

        @Test
        @DisplayName("Deve retornar agendamentos do cliente")
        void shouldReturnAppointmentsByClientId() {
            when(clientRepository.existsById(client.getId())).thenReturn(true);
            when(appointmentRepository.findAllByClientId(client.getId())).thenReturn(List.of(appointment));
            when(appointmentMapper.toResponse(appointment)).thenReturn(response);

            List<AppointmentResponse> result = appointmentService.findAllByClientId(client.getId());

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao buscar agendamentos de cliente inexistente")
        void shouldThrowNotFoundWhenClientNotFoundOnFindAllByClientId() {
            when(clientRepository.existsById(any())).thenReturn(false);

            assertThatThrownBy(() -> appointmentService.findAllByClientId(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cliente não encontrado");
        }
    }

    @Nested
    @DisplayName("findAllByProfessionalId")
    class FindAllByProfessionalId {

        @Test
        @DisplayName("Deve retornar agendamentos do profissional")
        void shouldReturnAppointmentsByProfessionalId() {
            when(professionalRepository.existsById(professional.getId())).thenReturn(true);
            when(appointmentRepository.findAllByProfessionalId(professional.getId())).thenReturn(List.of(appointment));
            when(appointmentMapper.toResponse(appointment)).thenReturn(response);

            List<AppointmentResponse> result = appointmentService.findAllByProfessionalId(professional.getId());

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao buscar agendamentos de profissional inexistente")
        void shouldThrowNotFoundWhenProfessionalNotFoundOnFindAllByProfessionalId() {
            when(professionalRepository.existsById(any())).thenReturn(false);

            assertThatThrownBy(() -> appointmentService.findAllByProfessionalId(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Profissional não encontrado");
        }
    }
}