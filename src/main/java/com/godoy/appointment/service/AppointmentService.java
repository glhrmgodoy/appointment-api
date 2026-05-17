package com.godoy.appointment.service;

import com.godoy.appointment.domain.entity.Appointment;
import com.godoy.appointment.domain.entity.Client;
import com.godoy.appointment.domain.entity.Professional;
import com.godoy.appointment.domain.entity.ServiceOffered;
import com.godoy.appointment.domain.enums.AppointmentStatus;
import com.godoy.appointment.dto.request.AppointmentRequest;
import com.godoy.appointment.dto.response.AppointmentResponse;
import com.godoy.appointment.exception.BusinessException;
import com.godoy.appointment.exception.NotFoundException;
import com.godoy.appointment.mapper.AppointmentMapper;
import com.godoy.appointment.repository.AppointmentRepository;
import com.godoy.appointment.repository.ClientRepository;
import com.godoy.appointment.repository.ProfessionalRepository;
import com.godoy.appointment.repository.ServiceOfferedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceOfferedRepository serviceOfferedRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentResponse create(AppointmentRequest request) {
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        if (!client.getActive()) {
            throw new BusinessException("Cliente inativo não pode realizar agendamentos");
        }

        Professional professional = professionalRepository.findById(request.professionalId())
                .orElseThrow(() -> new NotFoundException("Profissional não encontrado"));

        if (!professional.getActive()) {
            throw new BusinessException("Profissional inativo não pode receber agendamentos");
        }

        ServiceOffered service = serviceOfferedRepository.findById(request.serviceId())
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado"));

        if (!service.getProfessional().getId().equals(request.professionalId())) {
            throw new BusinessException("Serviço não pertence ao profissional informado");
        }

        if (!service.getActive()) {
            throw new BusinessException("Serviço inativo não pode ser agendado");
        }

        if (appointmentRepository.existsByProfessionalIdAndScheduledAt(
                request.professionalId(), request.scheduledAt())) {
            throw new BusinessException("Profissional já possui agendamento neste horário");
        }

        if (appointmentRepository.existsByClientIdAndScheduledAt(
                request.clientId(), request.scheduledAt())) {
            throw new BusinessException("Cliente já possui agendamento neste horário");
        }

        Appointment appointment = appointmentMapper.toEntity(request);
        appointment.setClient(client);
        appointment.setProfessional(professional);
        appointment.setService(service);
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    public List<AppointmentResponse> findAll() {
        return appointmentRepository.findAll()
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    public AppointmentResponse findById(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado"));

        return appointmentMapper.toResponse(appointment);
    }

    public List<AppointmentResponse> findAllByClientId(UUID clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new NotFoundException("Cliente não encontrado");
        }

        return appointmentRepository.findAllByClientId(clientId)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    public List<AppointmentResponse> findAllByProfessionalId(UUID professionalId) {
        if (!professionalRepository.existsById(professionalId)) {
            throw new NotFoundException("Profissional não encontrado");
        }

        return appointmentRepository.findAllByProfessionalId(professionalId)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    public AppointmentResponse confirm(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado"));

        if (!appointment.getStatus().equals(AppointmentStatus.PENDING)) {
            throw new BusinessException("Apenas agendamentos PENDING podem ser confirmados");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment update = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(update);
    }

    public AppointmentResponse complete(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado"));

        if (!appointment.getStatus().equals(AppointmentStatus.CONFIRMED)) {
            throw new BusinessException("Apenas agendamentos CONFIRMED podem ser concluídos");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment update = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(update);
    }

    public void cancel(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado"));

        if (appointment.getStatus().equals(AppointmentStatus.COMPLETED) ||
                appointment.getStatus().equals(AppointmentStatus.CONFIRMED)) {
            throw new BusinessException("Apenas agendamentos PENDING ou CONFIRMED podem ser cancelados");
        }

        if (appointment.getScheduledAt().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BusinessException("Cancelamento com menos de 2h de antecedência não é permitido");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }
}
