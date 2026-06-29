package com.parkvision.iam.interfaces.rest.internal;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de la introspección de API-key servicio-a-servicio: la key cruda {@code <keyId>.<secret>} que
 * un nodo Fog presentó a {@code occupancy-service} y que este reenvía a {@code iam} para validarla.
 */
public record ApiKeyIntrospectionRequest(@NotBlank String rawKey) {}
