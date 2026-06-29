package com.parkvision.iam.application.queryservices;

import com.parkvision.iam.domain.model.aggregates.ApiKey;
import com.parkvision.iam.domain.queries.GetAllApiKeysQuery;
import com.parkvision.iam.infrastructure.persistence.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiKeyQueryService {

    private final ApiKeyRepository apiKeyRepository;

    public List<ApiKey> handle(GetAllApiKeysQuery query) {
        return apiKeyRepository.findAll();
    }
}
