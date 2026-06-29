package com.parkvision.iam.interfaces.rest.internal;

import com.parkvision.iam.application.commandservices.ApiKeyCommandService;
import com.parkvision.iam.domain.exceptions.InvalidApiKeyException;
import com.parkvision.iam.domain.model.aggregates.ApiKey;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint interno (servicio-a-servicio) de introspección de API-key de nodos Fog. {@code iam} es dueño
 * de la tabla {@code api_key}, por lo que valida aquí la credencial cruda y devuelve su identidad. Lo
 * consume {@code occupancy-service} desde su {@code ApiKeyAuthenticationFilter}: si la respuesta es
 * {@code 200}, concede {@code ROLE_FOG}; si es {@code 401}, rechaza. No se publica en el gateway.
 */
@Hidden
@RestController
@RequestMapping(value = "/api/v1/iam/internal/api-keys", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiKeyIntrospectionController {

    private final ApiKeyCommandService apiKeyCommandService;

    @PostMapping(value = "/authenticate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiKeyIntrospectionResponse> authenticate(
            @Valid @RequestBody ApiKeyIntrospectionRequest request) {
        ApiKey apiKey = apiKeyCommandService.authenticate(request.rawKey())
                .orElseThrow(InvalidApiKeyException::new);
        return ResponseEntity.ok(new ApiKeyIntrospectionResponse(apiKey.getKeyId(), apiKey.getNodeId()));
    }
}
