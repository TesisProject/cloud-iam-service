package com.parkvision.shared.interfaces.rest;

import java.time.OffsetDateTime;

/**
 * Formato de error estándar de la API (ver CLAUDE.md §Manejo de errores).
 */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
