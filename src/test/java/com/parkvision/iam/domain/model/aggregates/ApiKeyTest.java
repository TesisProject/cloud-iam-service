package com.parkvision.iam.domain.model.aggregates;

import com.parkvision.iam.domain.commands.CreateApiKeyCommand;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyTest {

    private static ApiKey apiKey(Instant expiresAt) {
        return new ApiKey(new CreateApiKeyCommand("fog-miraflores", null, expiresAt), "fog_abc123", "hashed-secret");
    }

    @Test
    void createsActiveUsableKeyWithoutExpiration() {
        ApiKey key = apiKey(null);

        assertEquals("fog_abc123", key.getKeyId());
        assertEquals("hashed-secret", key.getSecretHash());
        assertEquals("fog-miraflores", key.getName());
        assertNull(key.getNodeId());
        assertNull(key.getExpiresAt());
        assertNull(key.getLastUsedAt());
        assertTrue(key.isActive());
        assertTrue(key.isUsable());
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApiKey(new CreateApiKeyCommand("  ", null, null), "fog_abc123", "hashed-secret"));
    }

    @Test
    void rejectsBlankKeyIdOrSecretHash() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApiKey(new CreateApiKeyCommand("fog-x", null, null), "  ", "hashed-secret"));
        assertThrows(IllegalArgumentException.class,
                () -> new ApiKey(new CreateApiKeyCommand("fog-x", null, null), "fog_abc123", "  "));
    }

    @Test
    void isNotUsableWhenExpired() {
        ApiKey key = apiKey(Instant.now().minus(1, ChronoUnit.MINUTES));
        assertFalse(key.isUsable());
    }

    @Test
    void isUsableWhenExpirationIsInTheFuture() {
        ApiKey key = apiKey(Instant.now().plus(1, ChronoUnit.HOURS));
        assertTrue(key.isUsable());
    }

    @Test
    void revokeMakesKeyUnusable() {
        ApiKey key = apiKey(null);
        key.revoke();
        assertFalse(key.isActive());
        assertFalse(key.isUsable());
    }

    @Test
    void recordUsageStampsLastUsedAt() {
        ApiKey key = apiKey(null);
        key.recordUsage();
        assertNotNull(key.getLastUsedAt());
    }

    @Test
    void exposesNoSetters() {
        boolean hasSetter = false;
        for (Method method : ApiKey.class.getDeclaredMethods()) {
            if (method.getName().startsWith("set")) {
                hasSetter = true;
                break;
            }
        }
        assertFalse(hasSetter, "ApiKey must not expose setters; the secret hash is immutable");
    }
}
