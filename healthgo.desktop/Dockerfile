# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . /app
RUN mvn -q -DskipTests package

# ---- Run stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Directory where CSV files will be read from (mounted volume)
VOLUME ["/data"]
# Backend URL can be configured via env
ENV BACKEND_URL=http://backend:8080
COPY --from=build /app/target/*.jar /app/desktop-client.jar
# By default, look for CSV inside /data. Adjust if your code expects a different path.
ENTRYPOINT ["sh", "-c", "java -jar /app/desktop-client.jar"]
