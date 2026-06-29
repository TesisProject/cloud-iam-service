package com.parkvision.iam.interfaces.assembler;

import com.parkvision.iam.domain.model.aggregates.ApiKey;
import com.parkvision.iam.interfaces.resource.ApiKeyResource;
import com.parkvision.iam.interfaces.resource.CreatedApiKeyResource;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyResourceAssembler {

    public ApiKeyResource toResource(ApiKey apiKey) {
        return new ApiKeyResource(
                apiKey.getId(),
                apiKey.getKeyId(),
                apiKey.getName(),
                apiKey.getNodeId(),
                apiKey.isActive(),
                apiKey.getExpiresAt(),
                apiKey.getLastUsedAt(),
                apiKey.getCreatedAt()
        );
    }

    public CreatedApiKeyResource toCreatedResource(ApiKey apiKey, String plainKey) {
        return new CreatedApiKeyResource(plainKey, toResource(apiKey));
    }
}
