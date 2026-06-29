package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.CreateApiKeyCommand;
import com.parkvision.iam.domain.model.aggregates.ApiKey;
import com.parkvision.iam.infrastructure.persistence.ApiKeyRepository;
import com.parkvision.iam.infrastructure.tokens.ApiKeyGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiKeyCommandServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;
    @Mock
    private ApiKeyGenerator apiKeyGenerator;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ApiKeyCommandService service;

    private static ApiKey existingKey(String keyId, String secretHash, Instant expiresAt) {
        return new ApiKey(new CreateApiKeyCommand("fog-test", null, expiresAt), keyId, secretHash);
    }

    @Test
    void createStoresHashedSecretAndReturnsPlainKeyOnce() {
        when(apiKeyGenerator.generateKeyId()).thenReturn("fog_test");
        when(apiKeyGenerator.generateSecret()).thenReturn("rawsecret");
        when(passwordEncoder.encode("rawsecret")).thenReturn("hashed");
        when(apiKeyGenerator.compose("fog_test", "rawsecret")).thenReturn("fog_test.rawsecret");
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> inv.getArgument(0));

        CreatedApiKey result = service.handle(new CreateApiKeyCommand("fog-test", 7L, null));

        ArgumentCaptor<ApiKey> captor = ArgumentCaptor.forClass(ApiKey.class);
        verify(apiKeyRepository).save(captor.capture());
        ApiKey saved = captor.getValue();
        // El secreto se persiste hasheado, nunca en claro
        assertEquals("hashed", saved.getSecretHash());
        assertNotEquals("rawsecret", saved.getSecretHash());
        assertEquals("fog_test", saved.getKeyId());
        assertEquals(7L, saved.getNodeId());
        // La key completa en claro se devuelve una sola vez
        assertEquals("fog_test.rawsecret", result.plainKey());
    }

    @Test
    void authenticateAcceptsValidKeyAndRecordsUsage() {
        ApiKey key = existingKey("fog_test", "hashed", null);
        when(apiKeyRepository.findByKeyId("fog_test")).thenReturn(Optional.of(key));
        when(passwordEncoder.matches("rawsecret", "hashed")).thenReturn(true);
        when(apiKeyRepository.save(any(ApiKey.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ApiKey> result = service.authenticate("fog_test.rawsecret");

        assertTrue(result.isPresent());
        assertNotNull(result.get().getLastUsedAt());
        verify(apiKeyRepository).save(key);
    }

    @Test
    void authenticateRejectsWrongSecret() {
        ApiKey key = existingKey("fog_test", "hashed", null);
        when(apiKeyRepository.findByKeyId("fog_test")).thenReturn(Optional.of(key));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertTrue(service.authenticate("fog_test.wrong").isEmpty());
        verify(apiKeyRepository, never()).save(any());
    }

    @Test
    void authenticateRejectsRevokedKey() {
        ApiKey key = existingKey("fog_test", "hashed", null);
        key.revoke();
        when(apiKeyRepository.findByKeyId("fog_test")).thenReturn(Optional.of(key));

        assertTrue(service.authenticate("fog_test.rawsecret").isEmpty());
        verify(apiKeyRepository, never()).save(any());
    }

    @Test
    void authenticateRejectsExpiredKey() {
        ApiKey key = existingKey("fog_test", "hashed", Instant.now().minus(1, ChronoUnit.MINUTES));
        when(apiKeyRepository.findByKeyId("fog_test")).thenReturn(Optional.of(key));

        assertTrue(service.authenticate("fog_test.rawsecret").isEmpty());
        verify(apiKeyRepository, never()).save(any());
    }

    @Test
    void authenticateRejectsUnknownKeyId() {
        when(apiKeyRepository.findByKeyId("fog_unknown")).thenReturn(Optional.empty());

        assertTrue(service.authenticate("fog_unknown.rawsecret").isEmpty());
        verify(apiKeyRepository, never()).save(any());
    }

    @Test
    void authenticateRejectsMalformedKey() {
        assertTrue(service.authenticate("no-separator").isEmpty());
        assertTrue(service.authenticate(null).isEmpty());
        verify(apiKeyRepository, never()).findByKeyId(any());
    }
}
