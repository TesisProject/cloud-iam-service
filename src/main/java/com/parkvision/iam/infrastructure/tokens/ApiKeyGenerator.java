package com.parkvision.iam.infrastructure.tokens;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Genera credenciales de API Key para los nodos Fog. La key entregada tiene el formato
 * {@code <keyId>.<secret>}: el {@code keyId} es público (lookup en BD) y el {@code secret} es la parte
 * sensible que solo se almacena hasheada. Espejo de {@link JwtTokenProvider} para credenciales de máquina.
 */
@Component
public class ApiKeyGenerator {

    public static final String KEY_ID_PREFIX = "fog_";
    public static final char SEPARATOR = '.';

    private static final int KEY_ID_BYTES = 6;   // 8 chars base64url tras el prefijo
    private static final int SECRET_BYTES = 32;  // 256 bits de entropía

    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    /** Identificador público corto e indexable, ej. {@code fog_a1b2c3d4}. */
    public String generateKeyId() {
        return KEY_ID_PREFIX + randomToken(KEY_ID_BYTES);
    }

    /** Secreto aleatorio de alta entropía; solo se almacena hasheado. */
    public String generateSecret() {
        return randomToken(SECRET_BYTES);
    }

    /** Compone la key completa entregada al cliente una sola vez: {@code <keyId>.<secret>}. */
    public String compose(String keyId, String secret) {
        return keyId + SEPARATOR + secret;
    }

    private String randomToken(int numBytes) {
        byte[] buffer = new byte[numBytes];
        secureRandom.nextBytes(buffer);
        return encoder.encodeToString(buffer);
    }
}
