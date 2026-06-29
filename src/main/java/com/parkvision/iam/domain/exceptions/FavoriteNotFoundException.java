package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.ResourceNotFoundException;

public class FavoriteNotFoundException extends ResourceNotFoundException {
    public FavoriteNotFoundException(Long userId, Long zoneId) {
        super("FAVORITE_NOT_FOUND", "Favorite not found for user " + userId + " and zone " + zoneId);
    }
}
