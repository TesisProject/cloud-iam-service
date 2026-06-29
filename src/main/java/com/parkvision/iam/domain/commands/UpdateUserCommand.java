package com.parkvision.iam.domain.commands;

public record UpdateUserCommand(Long userId, String email, String newPassword) {}
