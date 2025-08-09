# ---- Build stage ----
FROM node:20 AS build
WORKDIR /app
COPY . /app

# VITE_API_URL pode ser sobrescrito no build
ARG VITE_API_URL=/api
ENV VITE_API_URL=${VITE_API_URL}

RUN npm ci || npm install
RUN npm run build

# ---- Serve com Node ----
FROM node:20-alpine
WORKDIR /app

# Copia somente os arquivos de build
COPY --from=build /app /app

# Instala dependências necessárias para rodar o preview
RUN npm install -g serve

EXPOSE 5173
CMD ["serve", "-s", "dist", "-l", "5173"]
