# syntax=docker/dockerfile:1.7
# ── Build stage ───────────────────────────────────────────────────────────────
# Repo de un solo servicio: la raíz ES el módulo Maven. parkvision-shared se
# resuelve desde GitHub Packages, por lo que el build necesita un token de lectura
# que llega como build-secret (id=github_token) — nunca queda en la imagen.
#   docker build --secret id=github_token,env=GITHUB_TOKEN --build-arg GITHUB_ACTOR=$USER -t iam-service .
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

ARG GITHUB_ACTOR=token
ENV GITHUB_ACTOR=${GITHUB_ACTOR}

COPY . .
RUN --mount=type=secret,id=github_token \
    chmod +x mvnw && \
    GITHUB_TOKEN="$(cat /run/secrets/github_token)" \
    ./mvnw -q -s settings.xml package -DskipTests

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
