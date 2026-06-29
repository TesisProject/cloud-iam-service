package com.parkvision.iam.interfaces.resource;

/**
 * Respuesta de creación de una API Key. Contiene la key completa en claro ({@code apiKey}), que se
 * entrega <strong>una única vez</strong> y nunca se vuelve a exponer, junto con la metadata del recurso.
 */
public record CreatedApiKeyResource(
        String apiKey,
        ApiKeyResource metadata
) {}
