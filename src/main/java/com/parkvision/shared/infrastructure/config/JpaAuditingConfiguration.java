package com.parkvision.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activa la auditoría de Spring Data JPA: {@code @CreatedDate} y {@code @LastModifiedDate}
 * se rellenan automáticamente en cada persist/update vía {@code AuditingEntityListener}.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfiguration {
}
