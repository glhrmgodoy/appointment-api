package com.godoy.appointment.service;

import com.godoy.appointment.domain.entity.Client;
import com.godoy.appointment.dto.request.ClientRequest;
import com.godoy.appointment.dto.response.ClientResponse;
import com.godoy.appointment.exception.BusinessException;
import com.godoy.appointment.exception.NotFoundException;
import com.godoy.appointment.mapper.ClientMapper;
import com.godoy.appointment.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientResponse create(ClientRequest request) {
        if (clientRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        if (clientRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        Client client = clientMapper.toEntity(request);
        Client saved = clientRepository.save(client);

        return clientMapper.toResponse(saved);
    }

    public List<ClientResponse> findAll() {
        return clientRepository.findAll()
                .stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    public ClientResponse findById(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        return clientMapper.toResponse(client);
    }

    public ClientResponse update(UUID id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        if (!client.getEmail().equals(request.email()) &&
                clientRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado");
        }

        if (!client.getCpf().equals(request.cpf()) &&
                clientRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        client.setName(request.name());
        client.setEmail(request.email());
        client.setCpf(request.cpf());
        client.setPhone(request.phone());

        Client update = clientRepository.save(client);
        return clientMapper.toResponse(update);
    }

    public void delete(UUID id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        client.setActive(false);
        clientRepository.save(client);
    }
}
