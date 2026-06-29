package com.parkvision.iam.interfaces.resource;

public record AuthenticationResource(
        String token,
        String tokenType,
        Long userId,
        String email,
        String role
) {}
