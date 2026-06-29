package com.parkvision.iam.domain.exceptions;

import com.parkvision.shared.domain.exceptions.ResourceNotFoundException;

public class RoleNotFoundException extends ResourceNotFoundException {
    public RoleNotFoundException(Long roleId) {
        super("ROLE_NOT_FOUND", "Role not found: " + roleId);
    }
}
