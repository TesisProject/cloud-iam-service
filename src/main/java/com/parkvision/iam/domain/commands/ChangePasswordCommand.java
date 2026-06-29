package com.parkvision.iam.domain.commands;

public record ChangePasswordCommand(Long userId, String currentPassword, String newPassword) {}
