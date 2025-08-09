# ---- Build stage ----
FROM node:20 AS build
WORKDIR /app
COPY . /app
# VITE_API_URL can be overridden at build time: --build-arg VITE_API_URL=/api
ARG VITE_API_URL=/api
ENV VITE_API_URL=${VITE_API_URL}
RUN npm ci || npm install
RUN npm run build

# ---- Serve with nginx ----
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
