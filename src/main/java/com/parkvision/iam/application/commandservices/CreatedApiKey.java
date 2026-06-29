package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.model.aggregates.ApiKey;

/**
 * Resultado de crear una API Key: el aggregate persistido más la key completa en claro
 * ({@code <keyId>.<secret>}), que solo se entrega una vez en la creación y nunca se vuelve a exponer.
 */
public record CreatedApiKey(ApiKey apiKey, String plainKey) {}
