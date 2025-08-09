# HealthGo Docker Setup

## Requisitos
- Docker 24+ e Docker Compose v2

## Estrutura
- `docker-compose.yml` — orquestra Postgres, Backend, Frontend e (opcional) Desktop.
- `backend/Dockerfile` — build/run do Spring Boot.
- `frontend/Dockerfile` + `nginx.conf` — build do Vite e serve com NGINX (proxy /api).
- `desktop/Dockerfile` — build/run do cliente desktop (leitura de CSV).

## Como usar
1. Coloque seus `.csv` em `./csv/` (será montado no container do desktop em `/data`).
2. Suba os serviços:
   ```bash
   docker compose up -d --build
   ```
3. Acesse:
   - Backend (Swagger): http://localhost:8080/swagger-ui.html
   - Frontend: http://localhost:3000

> O frontend usa **/api** e o NGINX faz proxy para o backend, evitando problemas de CORS.

## Variáveis importantes
- Banco de dados:
  - `POSTGRES_DB=health-go`
  - `POSTGRES_USER=postgres`
  - `POSTGRES_PASSWORD=postgres`
- Backend (override por env):
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/health-go`
  - `SPRING_DATASOURCE_USERNAME=postgres`
  - `SPRING_DATASOURCE_PASSWORD=postgres`

## Observações
- Ajuste `JAVA_OPTS` no backend se precisar de mais memória.
- Se o Desktop requer caminho específico para CSV diferente de `/data`, ajuste o `ENTRYPOINT` ou o volume.
