# Appointment API

API REST de agendamento de serviços entre clientes e profissionais de saúde (dentistas, médicos, psicólogos, nutricionistas e fisioterapeutas). Cada profissional cadastra os serviços que oferece, com preço e duração, e os clientes agendam horários.

Projeto de portfólio focado em **regras de negócio**: conflito de horários, **máquina de estados** do agendamento, política de cancelamento e **exclusão lógica** (soft delete) de cadastros.

## Stack

- Java 21
- Spring Boot 4.0.6
- Spring Data JPA + Hibernate
- PostgreSQL 16
- MapStruct (mapeamento DTO ↔ Entity)
- Bean Validation
- Springdoc OpenAPI (Swagger UI)
- JUnit 5 + Mockito
- Docker Compose

## Regras de negócio

### Ciclo de vida do agendamento

```
            confirm              complete
PENDING ─────────────▶ CONFIRMED ─────────────▶ COMPLETED
   │                       │
   │        cancel         │
   └──────────┬────────────┘
              ▼
          CANCELLED
```

- Todo agendamento nasce como `PENDING`.
- Só um agendamento `PENDING` pode ser confirmado.
- Só um agendamento `CONFIRMED` pode ser concluído.
- Agendamentos `PENDING` ou `CONFIRMED` podem ser cancelados, **desde que faltem pelo menos 2 horas** para o horário marcado.

### Validações na criação do agendamento

- A data e a hora precisam estar no futuro.
- Cliente, profissional e serviço precisam existir e estar **ativos**.
- O serviço precisa **pertencer ao profissional** informado.
- **Conflito de horário**: nem o profissional nem o cliente podem ter outro agendamento no mesmo horário.

### Cadastros

- CPF e e-mail são únicos para clientes e profissionais; o CPF é validado com 11 dígitos.
- A exclusão é **lógica** (`active = false`), o que preserva o histórico de agendamentos.
- Um profissional com serviços ativos não pode ser inativado.
- Um profissional inativo não pode cadastrar serviços.
- O preço do serviço precisa ser maior que zero.

Violações de regra de negócio retornam `422`, e recursos inexistentes retornam `404`, ambos tratados por um `GlobalExceptionHandler`.

## Como rodar

### Pré-requisitos

- Java 21+
- Docker (para subir o PostgreSQL) ou uma instância própria do Postgres rodando

### 1. Variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto (ele **não** deve ser commitado):

```env
POSTGRES_DB=appointment_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=sua_senha_local
```

A aplicação lê as mesmas variáveis, então exporte-as no terminal ou configure-as na sua IDE antes de rodar.

### 2. Subir o banco de dados

```bash
docker compose up -d
```

### 3. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

As tabelas são criadas automaticamente pelo Hibernate na inicialização.

A API sobe em `http://localhost:8080`. A documentação interativa (Swagger UI) fica em `http://localhost:8080/swagger-ui.html`.

### 4. Rodar os testes

```bash
./mvnw test
```

## Endpoints principais

### Agendamentos

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/appointments` | Cria agendamento (status `PENDING`) |
| GET | `/api/v1/appointments` | Lista agendamentos |
| GET | `/api/v1/appointments/{id}` | Busca agendamento |
| GET | `/api/v1/appointments/client/{clientId}` | Agendamentos de um cliente |
| GET | `/api/v1/appointments/professional/{professionalId}` | Agenda de um profissional |
| PATCH | `/api/v1/appointments/{id}/confirm` | Confirma agendamento |
| PATCH | `/api/v1/appointments/{id}/complete` | Conclui agendamento |
| DELETE | `/api/v1/appointments/{id}` | Cancela agendamento (mínimo de 2h de antecedência) |

### Clientes e profissionais

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/clients` | Cadastra cliente |
| GET | `/api/v1/clients` | Lista clientes |
| GET | `/api/v1/clients/{id}` | Busca cliente |
| PUT | `/api/v1/clients/{id}` | Atualiza cliente |
| DELETE | `/api/v1/clients/{id}` | Inativa cliente |
| POST | `/api/v1/professionals` | Cadastra profissional |
| GET | `/api/v1/professionals` | Lista profissionais |
| GET | `/api/v1/professionals/{id}` | Busca profissional |
| PUT | `/api/v1/professionals/{id}` | Atualiza profissional |
| DELETE | `/api/v1/professionals/{id}` | Inativa profissional |

### Serviços

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/services` | Cadastra serviço de um profissional |
| GET | `/api/v1/services` | Lista serviços |
| GET | `/api/v1/services/{id}` | Busca serviço |
| GET | `/api/v1/services/professional/{professionalId}` | Serviços de um profissional |
| PUT | `/api/v1/services/{id}` | Atualiza serviço |
| DELETE | `/api/v1/services/{id}` | Inativa serviço |

### Exemplo: criar um agendamento

```http
POST /api/v1/appointments
Content-Type: application/json

{
  "clientId": "3f1c...",
  "professionalId": "a7b2...",
  "serviceId": "c9d4...",
  "scheduledAt": "2026-10-15T14:30:00",
  "notes": "Primeira consulta"
}
```

## Estrutura do projeto

```
src/main/java/com/godoy/appointment/
├── controller/      # Endpoints REST
├── service/         # Regras de negócio
├── repository/      # Interfaces Spring Data JPA
├── domain/
│   ├── entity/      # Client, Professional, ServiceOffered, Appointment
│   └── enums/       # AppointmentStatus, Specialty
├── dto/
│   ├── request/
│   └── response/
├── mapper/          # Interfaces MapStruct
└── exception/       # Exceções customizadas + GlobalExceptionHandler
```

## Testes

São 68 testes automatizados com JUnit 5 e Mockito, cobrindo os quatro services: os fluxos de sucesso e cada regra de negócio (conflito de horário, transições de status, antecedência mínima para cancelamento, cadastros inativos etc.).

## Status do projeto

- [x] Modelagem de domínio
- [x] Regras de negócio e máquina de estados
- [x] Tratamento global de exceções
- [x] Controllers REST + Swagger
- [x] Testes automatizados (68)
- [ ] Migrations versionadas com Flyway
- [ ] Paginação nas listagens
- [ ] Autenticação JWT
