package com.parkvision.iam.interfaces.rest;

import com.parkvision.iam.application.commandservices.ApiKeyCommandService;
import com.parkvision.iam.application.commandservices.CreatedApiKey;
import com.parkvision.iam.application.queryservices.ApiKeyQueryService;
import com.parkvision.iam.domain.commands.CreateApiKeyCommand;
import com.parkvision.iam.domain.commands.RevokeApiKeyCommand;
import com.parkvision.iam.domain.queries.GetAllApiKeysQuery;
import com.parkvision.iam.interfaces.assembler.ApiKeyResourceAssembler;
import com.parkvision.iam.interfaces.resource.ApiKeyResource;
import com.parkvision.iam.interfaces.resource.CreateApiKeyRequest;
import com.parkvision.iam.interfaces.resource.CreatedApiKeyResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Gestión de API Keys de los nodos Fog. Solo accesible por administradores humanos (JWT);
 * la autenticación de los Fog con la key emitida ocurre vía el header {@code X-API-Key}.
 */
@RestController
@RequestMapping(value = "/api/v1/iam/api-keys", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "API Keys", description = "Emisión y revocación de credenciales para nodos Fog")
@SecurityRequirement(name = "bearerAuth")
public class ApiKeyController {

    private final ApiKeyCommandService apiKeyCommandService;
    private final ApiKeyQueryService apiKeyQueryService;
    private final ApiKeyResourceAssembler apiKeyResourceAssembler;

    @PostMapping
    @Operation(summary = "Emitir una API Key para un nodo Fog (la key completa se muestra una sola vez)")
    public ResponseEntity<CreatedApiKeyResource> createApiKey(@Valid @RequestBody CreateApiKeyRequest request) {
        CreatedApiKey created = apiKeyCommandService.handle(
                new CreateApiKeyCommand(request.name(), request.nodeId(), request.expiresAt()));
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(created.apiKey().getId()).toUri();
        return ResponseEntity.created(location)
                .body(apiKeyResourceAssembler.toCreatedResource(created.apiKey(), created.plainKey()));
    }

    @GetMapping
    @Operation(summary = "Listar las API Keys emitidas (metadata, sin secretos)")
    public ResponseEntity<List<ApiKeyResource>> getAllApiKeys() {
        var apiKeys = apiKeyQueryService.handle(new GetAllApiKeysQuery())
                .stream().map(apiKeyResourceAssembler::toResource).toList();
        return ResponseEntity.ok(apiKeys);
    }

    @DeleteMapping("/{apiKeyId}")
    @Operation(summary = "Revocar una API Key")
    public ResponseEntity<Void> revokeApiKey(@PathVariable Long apiKeyId) {
        apiKeyCommandService.handle(new RevokeApiKeyCommand(apiKeyId));
        return ResponseEntity.noContent().build();
    }
}
