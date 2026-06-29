package com.parkvision.iam.domain.commands;

public record VerifyOtpCommand(String email, String otp) {}
