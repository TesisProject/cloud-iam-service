package com.parkvision.iam.interfaces.resource;

import java.time.LocalDateTime;

public record FavoriteResource(
        Long userId,
        Long zoneId,
        LocalDateTime savedAt
) {}
