package com.godoy.appointment.service;

import com.godoy.appointment.domain.entity.Professional;
import com.godoy.appointment.domain.entity.ServiceOffered;
import com.godoy.appointment.dto.request.ServiceOfferedRequest;
import com.godoy.appointment.dto.response.ServiceOfferedResponse;
import com.godoy.appointment.exception.BusinessException;
import com.godoy.appointment.exception.NotFoundException;
import com.godoy.appointment.mapper.ServiceOfferedMapper;
import com.godoy.appointment.repository.ProfessionalRepository;
import com.godoy.appointment.repository.ServiceOfferedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceOfferedService {

    private final ServiceOfferedRepository serviceOfferedRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceOfferedMapper serviceOfferedMapper;

    public ServiceOfferedResponse create(ServiceOfferedRequest request) {
        Professional professional = professionalRepository.findById(request.professionalId())
                .orElseThrow(() -> new NotFoundException("Profissional não encontrado"));

        if (!professional.getActive()) {
            throw new BusinessException("Profissional inativo não pode ter serviços");
        }

        ServiceOffered service = serviceOfferedMapper.toEntity(request);
        service.setProfessional(professional);

        ServiceOffered saved = serviceOfferedRepository.save(service);
        return serviceOfferedMapper.toResponse(saved);
    }

    public List<ServiceOfferedResponse> findAll() {
        return serviceOfferedRepository.findAll()
                .stream()
                .map(serviceOfferedMapper::toResponse)
                .toList();
    }

    public ServiceOfferedResponse findById(UUID id) {
        ServiceOffered service = serviceOfferedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado"));

        return serviceOfferedMapper.toResponse(service);
    }

    public List<ServiceOfferedResponse> findAllByProfessionalId(UUID professionalId) {
        if (!professionalRepository.existsById(professionalId)) {
            throw new NotFoundException("Profissional não encontrado");
        }

        return serviceOfferedRepository.findAllByProfessionalId(professionalId)
                .stream()
                .map(serviceOfferedMapper::toResponse)
                .toList();
    }

    public ServiceOfferedResponse update(UUID id, ServiceOfferedRequest request) {
        ServiceOffered service = serviceOfferedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado"));

        Professional professional = professionalRepository.findById(request.professionalId())
                .orElseThrow(() -> new NotFoundException("Profissional não encontrado"));

        if (!professional.getActive()) {
            throw new BusinessException("Profissional inativo não pode ter serviços");
        }

        service.setName(request.name());
        service.setDescription(request.description());
        service.setPrice(request.price());
        service.setDurationMinutes(request.durationMinutes());
        service.setProfessional(professional);

        ServiceOffered updated =  serviceOfferedRepository.save(service);
        return serviceOfferedMapper.toResponse(updated);
    }

    public void delete(UUID id) {
        ServiceOffered service = serviceOfferedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Serviço não encontrado"));

        service.setActive(false);
        serviceOfferedRepository.save(service);
    }
}
