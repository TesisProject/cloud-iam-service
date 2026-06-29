package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.ConflictException;

public class FavoriteAlreadyExistsException extends ConflictException {
    public FavoriteAlreadyExistsException(Long userId, Long zoneId) {
        super("FAVORITE_ALREADY_EXISTS", "Zone " + zoneId + " is already a favorite for user " + userId);
    }
}
