package com.parkvision.iam.interfaces.resource;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Representación de salida de una API Key. Expone solo metadata: nunca el secreto ni su hash.
 */
public record ApiKeyResource(
        Long id,
        String keyId,
        String name,
        Long nodeId,
        boolean active,
        Instant expiresAt,
        Instant lastUsedAt,
        LocalDateTime createdAt
) {}
