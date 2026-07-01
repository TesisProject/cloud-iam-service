# syntax=docker/dockerfile:1.7
# Imagen runtime del servicio. El jar se compila ANTES, fuera de Docker
# (`./mvnw package`). El codigo compartido va incluido en el repo, asi que
# no requiere credenciales ni GitHub Packages.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
