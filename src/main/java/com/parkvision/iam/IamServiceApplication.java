package com.parkvision.iam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Microservicio IAM de ParkVision: registro/login de usuarios, emisión de JWT (RS256), gestión de
 * roles/permisos/perfil/preferencias/favoritos/reseñas y API-keys de nodos Fog (con endpoint de
 * introspección que consume {@code occupancy-service}).
 *
 * <p>{@code @EnableAsync} habilita el envío asíncrono de emails (OTP de recuperación de contraseña).
 * El registro en Eureka se autoconfigura por la presencia del starter de Eureka client.
 */
@SpringBootApplication(scanBasePackages = {"com.parkvision.iam", "com.parkvision.shared"})
@EnableAsync
public class IamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamServiceApplication.class, args);
    }
}
