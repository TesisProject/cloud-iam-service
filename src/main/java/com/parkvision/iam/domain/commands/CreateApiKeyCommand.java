package com.parkvision.iam.domain.commands;

import java.time.Instant;

/**
 * Crea una API Key para un nodo Fog. No incluye id (lo genera la BD) ni el secreto (lo genera el service).
 *
 * @param name      etiqueta descriptiva del nodo (ej. {@code fog-miraflores}).
 * @param nodeId    referencia opcional al {@code Node} de parking (nullable por ahora).
 * @param expiresAt instante de expiración opcional; {@code null} = la key no expira.
 */
public record CreateApiKeyCommand(String name, Long nodeId, Instant expiresAt) {}
