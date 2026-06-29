package com.parkvision.iam.interfaces.resource;

import java.time.LocalDateTime;

public record RatingResource(
        Long userId,
        Long zoneId,
        Short stars,
        String comment,
        String type,
        boolean isReviewed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
