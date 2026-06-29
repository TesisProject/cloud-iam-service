package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.ResourceNotFoundException;

public class PermissionNotFoundException extends ResourceNotFoundException {
    public PermissionNotFoundException(Long permissionId) {
        super("PERMISSION_NOT_FOUND", "Permission not found: " + permissionId);
    }
}
