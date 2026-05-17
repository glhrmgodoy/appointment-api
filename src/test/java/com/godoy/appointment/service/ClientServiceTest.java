package com.godoy.appointment.service;

import com.godoy.appointment.domain.entity.Client;
import com.godoy.appointment.dto.request.ClientRequest;
import com.godoy.appointment.dto.response.ClientResponse;
import com.godoy.appointment.exception.BusinessException;
import com.godoy.appointment.exception.NotFoundException;
import com.godoy.appointment.mapper.ClientMapper;
import com.godoy.appointment.repository.ClientRepository;
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
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    private Client client;
    private ClientRequest request;
    private ClientResponse response;

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

        request = new ClientRequest(
                "Maria Clara",
                "mariaclara@email.com",
                "12345678910",
                "11999999999"
        );

        response = new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getCpf(),
                client.getPhone(),
                null
        );
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar cliente com sucesso")
        void shouldCreateClientSuccessfully() {
            when(clientRepository.existsByEmail(request.email())).thenReturn(false);
            when(clientRepository.existsByCpf(request.cpf())).thenReturn(false);
            when(clientMapper.toEntity(request)).thenReturn(client);
            when(clientRepository.save(any())).thenReturn(client);
            when(clientMapper.toResponse(client)).thenReturn(response);

            ClientResponse result = clientService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Maria Clara");
            assertThat(result.email()).isEqualTo("mariaclara@email.com");
            verify(clientRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando Email já cadastrado")
        void shouldThrowBusinessExceptionWhenEmailAlreadyExists() {
            when(clientRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() ->  clientService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email já cadastrado");

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando CPF já cadastrado")
        void shouldThrowBusinessExceptionWhenCpfAlreadyExists() {
            when(clientRepository.existsByEmail(request.email())).thenReturn(false);
            when(clientRepository.existsByCpf(request.cpf())).thenReturn(true);

            assertThatThrownBy(() ->  clientService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("CPF já cadastrado");

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar cliente por ID com sucesso")
        void shouldFindClientByIdSuccessfully() {
            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
            when(clientMapper.toResponse(client)).thenReturn(response);

            ClientResponse result = clientService.findById(client.getId());

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(client.getId());
            assertThat(result.name()).isEqualTo("Maria Clara");
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando cliente não encontrado")
        void shouldThrowNotFoundWhenClientNotFound() {
            when(clientRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.findById(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cliente não encontrado");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar lista de clientes")
        void shouldReturnListOfClients() {
            when(clientRepository.findAll()).thenReturn(List.of(client));
            when(clientMapper.toResponse(client)).thenReturn(response);

            List<ClientResponse> result = clientService.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().name()).isEqualTo("Maria Clara");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há clientes")
        void shouldReturnEmptyListWhenNoClients() {
            when(clientRepository.findAll()).thenReturn(List.of());

            List<ClientResponse> result = clientService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar cliente com sucesso")
        void shouldUpdateClientSuccessfully() {
            client.setEmail("email@email.com");
            client.setCpf("99999999999");
            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
            when(clientRepository.existsByEmail(request.email())).thenReturn(false);
            when(clientRepository.existsByCpf(request.cpf())).thenReturn(false);
            when(clientRepository.save(any())).thenReturn(client);
            when(clientMapper.toResponse(client)).thenReturn(response);

            ClientResponse result = clientService.update(client.getId(), request);

            assertThat(result).isNotNull();
            verify(clientRepository).save(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException ao atualizar cliente inexistente")
        void shouldThrowNotFoundUpdatingNonExistentClient() {
            when(clientRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.update(UUID.randomUUID(), request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cliente não encontrado");

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar com email já usado por outro cliente")
        void shouldThrowBusinessExceptionWhenEmailUsedByAnotherClient() {
            client.setEmail("email@email.com");
            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
            when(clientRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> clientService.update(client.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email já cadastrado");

            verify(clientRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException ao atualizar com CPF já usado por outro cliente")
        void shouldThrowBusinessExceptionWhenCpfUsedByAnotherClient() {
            client.setCpf("99999999999");
            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
            when(clientRepository.existsByCpf(request.cpf())).thenReturn(true);

            assertThatThrownBy(() -> clientService.update(client.getId(), request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("CPF já cadastrado");

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve inativar cliente com sucesso")
        void shouldDeleteClientSuccessfully() {
            when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));

            clientService.delete(client.getId());

            verify(clientRepository).save(argThat(c -> !c.getActive()));
        }

        @Test
        @DisplayName("Devlançar NotFoundException ao inativar cliente inexistente")
        void shouldThrowNotFoundWhenDeletingNonExistentClient() {
            when(clientRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.delete(UUID.randomUUID()))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cliente não encontrado");

            verify(clientRepository, never()).save(any());
        }
    }

}