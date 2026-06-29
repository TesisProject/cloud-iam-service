package com.parkvision.iam.infrastructure.config;

import com.parkvision.iam.application.commandservices.UserCommandService;
import com.parkvision.iam.domain.commands.SignUpCommand;
import com.parkvision.iam.domain.model.aggregates.Role;
import com.parkvision.iam.infrastructure.persistence.RoleRepository;
import com.parkvision.iam.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserCommandService userCommandService;

    private static final List<String[]> DEFAULT_ROLES = List.of(
            new String[]{"ADMIN",    "Administrador del sistema con acceso total"},
            new String[]{"OPERATOR", "Operador con acceso a gestión de estacionamientos"},
            new String[]{"USER",     "Usuario final de la aplicación ParkVision"},
            new String[]{"FOG",      "Nodo Fog (máquina) que reporta eventos de ocupación vía API Key"}
    );

    // Credenciales del usuario de prueba para Swagger / desarrollo local
    private static final String DEV_ADMIN_EMAIL    = "admin@parkvision.dev";
    private static final String DEV_ADMIN_PASSWORD = "Admin1234!";

    @Override
    public void run(String... args) {
        DEFAULT_ROLES.forEach(entry -> {
            String name = entry[0];
            String description = entry[1];
            if (!roleRepository.existsByName(name)) {
                roleRepository.save(new Role(name, description));
            }
        });

        if (!userRepository.existsByEmail(DEV_ADMIN_EMAIL)) {
            userCommandService.handle(new SignUpCommand(DEV_ADMIN_EMAIL, DEV_ADMIN_PASSWORD, "ADMIN"));
        }
    }
}
