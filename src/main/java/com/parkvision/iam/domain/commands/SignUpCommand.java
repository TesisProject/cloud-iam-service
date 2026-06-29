package com.parkvision.iam.domain.commands;

public record SignUpCommand(String email, String password, String roleName) {}
