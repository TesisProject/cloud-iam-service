package com.parkvision.iam.application.internal;

import java.time.LocalDateTime;

/**
 * Vista de solo lectura de una reseña de zona, expuesta por {@code iam} hacia otros bounded contexts
 * (p.ej. {@code parking}) a través de {@link IamEngagementFacade}. El {@code displayName} ya viene
 * resuelto y el email enmascarado: {@code iam} es dueño de {@code User}/{@code UserProfile} y esos
 * datos nunca salen crudos del contexto.
 */
public record ZoneRatingView(
        Long userId,
        String displayName,
        Short stars,
        String comment,
        String type,
        LocalDateTime createdAt
) {}
