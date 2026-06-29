package com.parkvision.iam.interfaces.resource;

public record PermissionResource(
        Long id,
        String name,
        String resource,
        String action,
        String description
) {}
