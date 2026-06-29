package com.parkvision.iam.infrastructure.config;

import com.parkvision.iam.application.commandservices.ApiKeyCommandService;
import com.parkvision.iam.application.commandservices.CreatedApiKey;
import com.parkvision.iam.domain.commands.CreateApiKeyCommand;
import com.parkvision.iam.infrastructure.persistence.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Crea la API key de prueba para el nodo Fog al arrancar en perfil {@code dev}.
 * La key se genera una única vez (idempotente) y se imprime en el log.
 * <p>
 * Copia el valor {@code X-API-Key} del log y úsalo en las peticiones a
 * {@code POST /api/v1/occupancy/events} y {@code POST /api/v1/occupancy/cameras/{id}/events}.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevApiKeySeeder implements ApplicationRunner {

    private static final String DEV_KEY_NAME = "API Key Dev Fog";

    private final ApiKeyCommandService apiKeyCommandService;
    private final ApiKeyRepository apiKeyRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (apiKeyRepository.existsByName(DEV_KEY_NAME)) {
            log.info("[DevApiKeySeeder] API key '{}' ya existe — omitiendo creación.", DEV_KEY_NAME);
            return;
        }

        CreatedApiKey created = apiKeyCommandService.handle(
                new CreateApiKeyCommand(DEV_KEY_NAME, null, null));

        log.warn("""

                ╔══════════════════════════════════════════════════════════════╗
                ║  [DEV] API KEY FOG — solo visible esta vez                  ║
                ║                                                              ║
                ║  Header:  X-API-Key: {}
                ║                                                              ║
                ║  Úsala en POST /api/v1/occupancy/events                     ║
                ║       y  POST /api/v1/occupancy/cameras/{{id}}/events        ║
                ╚══════════════════════════════════════════════════════════════╝
                """, created.plainKey());
    }
}
