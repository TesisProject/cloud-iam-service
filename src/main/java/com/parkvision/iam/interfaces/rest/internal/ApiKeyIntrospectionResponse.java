package com.parkvision.iam.interfaces.rest.internal;

/**
 * Resultado de una introspección de API-key válida: identidad pública de la credencial. {@code keyId} es
 * el principal que {@code occupancy-service} usará para la autenticación {@code ROLE_FOG}; {@code nodeId}
 * vincula la key al nodo Fog (puede ser {@code null}).
 */
public record ApiKeyIntrospectionResponse(String keyId, Long nodeId) {}
