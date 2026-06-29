package com.parkvision.iam.infrastructure.tokens;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyGeneratorTest {

    private final ApiKeyGenerator generator = new ApiKeyGenerator();

    @Test
    void keyIdCarriesFogPrefix() {
        assertTrue(generator.generateKeyId().startsWith(ApiKeyGenerator.KEY_ID_PREFIX));
    }

    @Test
    void secretIsHighEntropy() {
        String secret = generator.generateSecret();
        // 32 bytes en base64url sin padding ≈ 43 chars
        assertTrue(secret.length() >= 40, "secret should encode 256 bits of entropy");
    }

    @Test
    void generatesUniqueValuesAcrossCalls() {
        assertNotEquals(generator.generateKeyId(), generator.generateKeyId());
        assertNotEquals(generator.generateSecret(), generator.generateSecret());
    }

    @Test
    void composeJoinsKeyIdAndSecretWithSeparator() {
        String composed = generator.compose("fog_abc123", "supersecret");
        assertEquals("fog_abc123" + ApiKeyGenerator.SEPARATOR + "supersecret", composed);
        assertEquals(2, composed.split("\\" + ApiKeyGenerator.SEPARATOR).length);
    }
}
