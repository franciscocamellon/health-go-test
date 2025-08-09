# Arquitetura - HealthGo

## Visão Geral

O HealthGo é uma solução para ingestão, processamento e visualização de dados vitais de pacientes, projetada para operar em tempo real e garantir **segurança, escalabilidade e compliance com LGPD**.  
A arquitetura segue princípios de **Domain-Driven Design (DDD)** e **Clean Architecture**, garantindo separação clara de responsabilidades e facilitando manutenção e evolução.

---

## 1. Componentes Principais

### 1.1 Backend - API de Ingestão e Consulta

- **Framework**: Spring Boot 3.x
- **Padrão Arquitetural**: RESTful com autenticação JWT
- **Banco de Dados**: PostgreSQL (estrutura normalizada, índices para consultas por `patient_id` e `timestamp`)
- **Persistência**: Hibernate/JPA
- **Segurança**:
  - Spring Security com roles (`ROLE_DOCTOR`, `ROLE_VISITOR`)
  - Filtragem de campos sensíveis no retorno
  - CORS configurado para permitir apenas origens confiáveis
- **Logs & Auditoria**: SLF4J + Logback, com rastreamento de requisições e exclusão de PII nos logs
- **Documentação**: Swagger/OpenAPI 3.0

Fluxo resumido de ingestão:

1. Desktop Client envia POST `/api/v1/patients/ingest`
2. Backend valida JWT e permissões
3. Dados são validados e convertidos em `VitalEntity`
4. Registro é persistido no banco
5. Evento é disparado para atualização do cache (futuro: WebSocket)

---

### 1.2 Desktop Client - Ingestão Automática de CSV

- **Linguagem**: Java 17
- **Responsabilidade**:
  - Leitura incremental de arquivos CSV
  - Conversão de valores para tipos corretos (`int`, `double`, `LocalTime`)
  - Envio assíncrono e concorrente de múltiplos pacientes (`ExecutorService`)
- **Validações**:
  - Conversão segura via `safeInt` e `safeD` para evitar falhas de parse
  - Normalização de campos (ex.: status padrão `NORMAL`)
- **Tolerância a Falhas**:
  - Retentativas automáticas em caso de falha de rede
  - Log de erros HTTP (ex.: 400, 403, 404)

---

### 1.3 Frontend - Dashboard de Pacientes

- **Framework**: React 18
- **UI**: Material UI v5
- **Design**:
  - Layout responsivo (Grid + Flexbox)
  - Exibição condicional de dados sensíveis (`showPII`)
  - Uso de `Chip` para alertas
- **Gráficos**:
  - Componente `Sparkline` para visualização rápida de tendências
  - Ajustado para receber dados diretos do backend, sem depender de séries mockadas
- **Integração com API**:
  - `Axios` para consumo REST
  - Token JWT armazenado e enviado no header `Authorization`
- **Evolução planejada**:
  - Migrar polling para WebSocket para atualização em tempo real
  - Implementar filtros por status e faixa de tempo

---

## 2. Decisões Técnicas e Trade-offs

| Decisão                                    | Justificativa                                                                    | Trade-off                                           |
| ------------------------------------------ | -------------------------------------------------------------------------------- | --------------------------------------------------- |
| **JWT com expiração curta (15 min)**       | Maior segurança contra sequestro de sessão                                       | Requer refresh token ou re-login frequente          |
| **PostgreSQL**                             | Confiabilidade, suporte a tipos numéricos precisos e consultas temporais rápidas | Sobrecarga de configuração comparado a bancos NoSQL |
| **Envio CSV por Desktop Client**           | Simplicidade de integração com dispositivos que já exportam CSV                  | Depende de execução local; não é full cloud         |
| **Polling no frontend**                    | Simples de implementar e controlar                                               | Não é tão eficiente quanto WebSocket                |
| **Conversão segura (`safeInt` / `safeD`)** | Evita exceções em dados malformados                                              | Pode gerar dados nulos se conversão falhar          |

---

## 3. Fluxos de Dados

### 3.1 Ingestão

CSV -> Desktop Client -> API /api/v1/patients/ingest -> Service -> Repository -> PostgreSQL

### 3.2 Visualização

React Dashboard -> GET /api/v1/patients -> JSON -> Renderização no PatientCard e Sparkline

---

## 4. Escalabilidade e Evolução

- **Backend**:
  - Adição de WebSocket para push de dados
  - Cache Redis para consultas frequentes
- **Frontend**:
  - WebSocket + Context API para atualização instantânea
- **Desktop Client**:
  - Capacidade de consumir múltiplos formatos (JSON, XLSX)
  - Upload direto para S3 em caso de indisponibilidade da API

---

## 5. Segurança

- Tráfego exclusivamente HTTPS
- Filtragem e sanitização de entrada no backend
- Criptografia de dados sensíveis no banco
- Logs sem dados pessoais (PII)

---

## 6. Compliance

- Vide documento `LGPD_COMPLIANCE.md` para práticas específicas adotadas
