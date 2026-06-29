package com.parkvision.iam.infrastructure.persistence;

import com.parkvision.iam.domain.model.aggregates.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyId(String keyId);
    boolean existsByName(String name);
}
