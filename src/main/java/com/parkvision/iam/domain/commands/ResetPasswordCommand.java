package com.parkvision.iam.domain.commands;

public record ResetPasswordCommand(String email, String otp, String newPassword) {}
