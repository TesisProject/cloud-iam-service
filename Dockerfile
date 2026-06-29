# ── Build stage ───────────────────────────────────────────────────────────────
# El contexto de build debe ser la RAÍZ del repositorio (reactor Maven multi-módulo).
# Se compila solo este servicio + sus dependencias locales con -pl <módulo> -am.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY . .
RUN chmod +x mvnw && ./mvnw -q -pl iam-service -am package -DskipTests

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /workspace/iam-service/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
