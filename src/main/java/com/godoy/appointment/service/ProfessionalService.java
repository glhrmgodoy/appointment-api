package com.godoy.appointment.service;

import com.godoy.appointment.domain.entity.Professional;
import com.godoy.appointment.dto.request.ProfessionalRequest;
import com.godoy.appointment.dto.response.ProfessionalResponse;
import com.godoy.appointment.exception.BusinessException;
import com.godoy.appointment.exception.NotFoundException;
import com.godoy.appointment.mapper.ProfessionalMapper;
import com.godoy.appointment.repository.ProfessionalRepository;
import com.godoy.appointment.repository.ServiceOfferedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final ServiceOfferedRepository serviceOfferedRepository;
    private final ProfessionalMapper professionalMapper;

    public ProfessionalResponse create(ProfessionalRequest request) {
        if (professionalRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        if (professionalRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        Professional professional = professionalMapper.toEntity(request);
        Professional saved = professionalRepository.save(professional);
        return professionalMapper.toResponse(saved);
    }

    public List<ProfessionalResponse> findAll() {
        return professionalRepository.findAll()
                .stream()
                .map(professionalMapper::toResponse)
                .toList();
    }

    public ProfessionalResponse findById(UUID id) {
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profissional não encontrado"));

        return professionalMapper.toResponse(professional);
    }

    public ProfessionalResponse update(UUID id, ProfessionalRequest request) {
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profissional não encontrado"));

        if (!professional.getEmail().equals(request.email()) &&
                professionalRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        if (!professional.getCpf().equals(request.cpf()) &&
                professionalRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        professional.setName(request.name());
        professional.setEmail(request.email());
        professional.setCpf(request.cpf());
        professional.setPhone(request.phone());
        professional.setSpecialty(request.specialty());

        Professional update = professionalRepository.save(professional);
        return professionalMapper.toResponse(update);
    }

    public void delete(UUID id) {
        Professional professional = professionalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profissional não encontrado"));

        if (serviceOfferedRepository.existsByProfessionalIdAndActiveTrue(id)) {
            throw new BusinessException("Profissional possui serviços ativos e não pode ser inativado");
        }

        professional.setActive(false);
        professionalRepository.save(professional);
    }
}
