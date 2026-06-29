package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.commands.CreateApiKeyCommand;
import com.parkvision.iam.domain.commands.RevokeApiKeyCommand;
import com.parkvision.iam.domain.exceptions.ApiKeyNotFoundException;
import com.parkvision.iam.domain.model.aggregates.ApiKey;
import com.parkvision.iam.infrastructure.persistence.ApiKeyRepository;
import com.parkvision.iam.infrastructure.tokens.ApiKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiKeyCommandService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyGenerator apiKeyGenerator;
    private final PasswordEncoder passwordEncoder;

    /**
     * Genera una API Key, almacena únicamente el hash del secreto y devuelve la key completa en claro
     * (entregable una sola vez).
     */
    @Transactional
    public CreatedApiKey handle(CreateApiKeyCommand command) {
        String keyId = apiKeyGenerator.generateKeyId();
        String secret = apiKeyGenerator.generateSecret();
        String secretHash = passwordEncoder.encode(secret);

        ApiKey apiKey = new ApiKey(command, keyId, secretHash);
        apiKeyRepository.save(apiKey);

        return new CreatedApiKey(apiKey, apiKeyGenerator.compose(keyId, secret));
    }

    @Transactional
    public ApiKey handle(RevokeApiKeyCommand command) {
        ApiKey apiKey = apiKeyRepository.findById(command.apiKeyId())
                .orElseThrow(() -> new ApiKeyNotFoundException(command.apiKeyId()));
        apiKey.revoke();
        return apiKeyRepository.save(apiKey);
    }

    /**
     * Valida una key cruda {@code <keyId>.<secret>}: localiza por {@code keyId}, comprueba que sea
     * usable (activa y no expirada) y que el secreto coincida con el hash. En éxito registra el uso.
     * Devuelve {@code Optional.empty()} ante cualquier entrada inválida (sin lanzar excepción), para que
     * el filtro de seguridad decida la respuesta.
     */
    @Transactional
    public Optional<ApiKey> authenticate(String rawKey) {
        if (rawKey == null) {
            return Optional.empty();
        }
        int separator = rawKey.indexOf(ApiKeyGenerator.SEPARATOR);
        if (separator <= 0 || separator == rawKey.length() - 1) {
            return Optional.empty();
        }
        String keyId = rawKey.substring(0, separator);
        String secret = rawKey.substring(separator + 1);

        return apiKeyRepository.findByKeyId(keyId)
                .filter(ApiKey::isUsable)
                .filter(apiKey -> passwordEncoder.matches(secret, apiKey.getSecretHash()))
                .map(apiKey -> {
                    apiKey.recordUsage();
                    return apiKeyRepository.save(apiKey);
                });
    }
}
