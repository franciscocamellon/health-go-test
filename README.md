# HealthGo

> Plataforma integrada para **ingestão**, **processamento** e **visualização** de dados vitais de pacientes em **tempo real**, com foco em **segurança**, **escalabilidade** e **conformidade com a LGPD**.

- Arquitetura detalhada: [ARCHITECTURE.md](ARCHITECTURE.md)
- Práticas de privacidade: [LGPD_COMPLIANCE.md](LGPD_COMPLIANCE.md)

---

## Visão Geral do Repositório

```
.
├── backend/         # API REST (Spring Boot)
├── frontend/        # Dashboard (React + MUI)
└── desktop-client/  # Ingestão automática de CSV (Java 17)
```

**Módulos**

1. **Backend** – API de ingestão/consulta, autenticação JWT, PostgreSQL.
2. **Frontend** – Dashboard responsivo com gráficos e alertas.
3. **Desktop Client** – Leitura de CSV e envio assíncrono para a API.

---

## Quickstart

### Pré-requisitos

- **Java 21+**
- **Maven 3.9+**
- **Node.js 20+**
- **PostgreSQL 14+**

### Setup do Banco (local)

Crie o banco e um usuário para a aplicação:

```sql
CREATE DATABASE healthgo;
CREATE USER healthgo_user WITH ENCRYPTED PASSWORD 'change_me';
GRANT ALL PRIVILEGES ON DATABASE healthgo TO healthgo_user;
```

---

## Configuração de Ambiente

### Backend (`backend/src/main/resources/application.properties`)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/healthgo
spring.datasource.username=healthgo_user
spring.datasource.password=change_me

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

# Segurança
app.security.jwt.secret=CHANGE_ME_SUPER_SECRET
app.security.jwt.expiration-minutes=15

# CORS (ajuste conforme seu domínio)
app.cors.allowed-origins=http://localhost:5173,https://www.seudominio.com
```

> Dica: você pode usar perfis (`application-dev.properties`, `application-prod.properties`) e variáveis de ambiente para separar **dev/staging/prod**.

### Frontend (`frontend/.env`)

```env
VITE_API_BASE_URL=http://localhost:8080
```

### Desktop Client

- Lê `.csv` de `desktop-client/src/main/resources/data/` e envia para o backend.
- Execute na mesma rede do backend (ou ajuste a URL no código/config do cliente).

---

## Como Executar

### 1) Backend

```bash
cd backend
mvn spring-boot:run
```

- **API:** http://localhost:8080
- **Swagger:** http://localhost:8080/swagger-ui.html

### 2) Frontend

```bash
cd frontend
npm install
npm run dev
```

- **App:** http://localhost:5173

### 3) Desktop Client

```bash
cd desktop-client
mvn clean package
java -jar target/desktop-client.jar
```

- Leitura contínua de `.csv` em `resources/data/` e envio para a API.

---

## 📚 Documentação da API (resumo)

> Documentação completa no Swagger: **/swagger-ui.html**

| Método | Rota                           | Descrição                                  | Autenticação |
| -----: | ------------------------------ | ------------------------------------------ | :----------: |
|   POST | `/api/v1/auth/login`           | Autentica e retorna JWT                    |     —/✔︎     |
|    GET | `/api/v1/patients`             | Lista pacientes (filtros por status/tempo) |      ✔︎      |
|    GET | `/api/v1/patients/{patientId}` | Dados do paciente                          |      ✔︎      |
|    GET | `/api/v1/patients/{id}/vitals` | Séries de sinais vitais (janela tempo)     |      ✔︎      |
|   POST | `/api/v1/patients/ingest`      | Ingestão (Desktop Client/CSV/JSON)         |      ✔︎      |

### Exemplos

**Login**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login   -H "Content-Type: application/json"   -d '{"username":"medico","password":"medico"}'
```

**Listar pacientes**

```bash
curl -X GET "http://localhost:8080/api/v1/patients?status=ALERT&limit=20"   -H "Authorization: Bearer <TOKEN>"
```

**Ingestão (JSON)**

```bash
curl -X POST http://localhost:8080/api/v1/patients/ingest   -H "Authorization: Bearer <TOKEN>"   -H "Content-Type: application/json"   -d '[{
    "patientId":"PAC01",
    "heartRate":78,
    "spo2":97,
    "temperature":36.6,
    "timestamp":"2025-08-09T10:00:00Z"
  }]'
```

> Campos sensíveis podem ser filtrados no retorno conforme permissões (ex.: `ROLE_DOCTOR`).

---

## Qualidade & Dev Experience

### Testes

- **Backend:** `mvn test`
- **Frontend:** `npm test` (se configurado)

### Convenções

- **Commits:** Conventional Commits (`feat:`, `fix:`, `chore:`…)
- **Estilo:** Linter/Hooks (recomendado: **Spotless** no Java, **ESLint/Prettier** no front)
- **Branches:** `main` (estável), `dev` (integração), `feature/*` (novas features)

---

## Segurança & LGPD

- Autenticação **JWT** com expiração curta e refresh (se habilitado)
- Logs sem PII; criptografia em repouso e em trânsito (HTTPS)
- Controles de acesso por role (ex.: `ROLE_DOCTOR`, `ROLE_ADMIN`)
- Detalhes e responsabilidades: **[LGPD_COMPLIANCE.md](LGPD_COMPLIANCE.md)**

---

## 📄 Licença

Uso **interno** da organização. Verifique políticas internas antes de redistribuir.

---

### Anexos

- Arquitetura: [ARCHITECTURE.md](ARCHITECTURE.md)
- LGPD: [LGPD_COMPLIANCE.md](LGPD_COMPLIANCE.md)
