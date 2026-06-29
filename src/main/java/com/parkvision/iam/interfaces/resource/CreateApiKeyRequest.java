package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * Cuerpo para crear una API Key de un nodo Fog.
 *
 * @param name      etiqueta descriptiva del nodo (ej. {@code fog-miraflores}).
 * @param nodeId    referencia opcional al {@code Node} de parking (nullable por ahora).
 * @param expiresAt expiración opcional; {@code null} = la key no expira.
 */
public record CreateApiKeyRequest(
        @NotBlank String name,
        Long nodeId,
        Instant expiresAt
) {}
