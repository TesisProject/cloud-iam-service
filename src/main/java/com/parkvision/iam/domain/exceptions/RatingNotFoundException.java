package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.ResourceNotFoundException;

public class RatingNotFoundException extends ResourceNotFoundException {
    public RatingNotFoundException(Long userId, Long zoneId) {
        super("RATING_NOT_FOUND", "Rating not found for user " + userId + " and zone " + zoneId);
    }
}
