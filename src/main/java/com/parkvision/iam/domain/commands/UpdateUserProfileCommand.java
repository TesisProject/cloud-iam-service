package com.parkvision.iam.domain.commands;

public record UpdateUserProfileCommand(
        Long userId,
        String firstName,
        String lastName,
        String phone,
        String avatarUrl,
        String bio
) {}
