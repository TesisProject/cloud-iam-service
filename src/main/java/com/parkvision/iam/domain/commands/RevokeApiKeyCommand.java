package com.parkvision.iam.domain.commands;

/**
 * Revoca (desactiva) una API Key existente.
 *
 * @param apiKeyId id de la API Key a revocar.
 */
public record RevokeApiKeyCommand(Long apiKeyId) {}
