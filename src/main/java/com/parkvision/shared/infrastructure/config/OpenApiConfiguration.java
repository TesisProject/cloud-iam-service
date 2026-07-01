package com.parkvision.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
public class OpenApiConfiguration {

    // Orden de etiquetas por bounded context. Cada microservicio expone solo su
    // subconjunto; el customizer únicamente ordena las que existen en cada Swagger.
    private static final List<String> TAG_ORDER = List.of(
            // iam
            "Authentication",
            "Users",
            "User Profile",
            "User Preferences",
            "Roles",
            "Permissions",
            "API Keys",
            "Favorites",
            "Ratings",
            // parking
            "Zones",
            "Spaces",
            // occupancy
            "Events",
            "Cameras",
            "Nodes",
            // prediction
            "Forecasts",
            "Zone forecasts",
            // notifications
            "Push",
            "Camera Alerts"
    );

    @Bean
    public OpenAPI parkVisionOpenAPI() {
        return new OpenAPI()
                // Server relativo: el "Try it out" usa el origen desde el que se cargó el Swagger
                // (el gateway en :8080, o el puerto directo del servicio en dev), no la IP interna
                // del contenedor que springdoc generaría por defecto.
                .servers(List.of(new Server().url("/").description("Mismo origen (gateway o servicio)")))
                .info(new Info()
                        .title("ParkVision API")
                        .description("Backend de gestión inteligente de estacionamientos — ParkVision")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0"))
                        .contact(new Contact().name("ParkVision Team")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes("apiKeyAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")));
    }

    @Bean
    public OpenApiCustomizer tagsOrderCustomizer() {
        return openApi -> {
            if (openApi.getTags() == null) return;
            openApi.getTags().sort(Comparator.comparingInt(
                    tag -> {
                        int idx = TAG_ORDER.indexOf(tag.getName());
                        return idx == -1 ? Integer.MAX_VALUE : idx;
                    }
            ));
        };
    }
}
