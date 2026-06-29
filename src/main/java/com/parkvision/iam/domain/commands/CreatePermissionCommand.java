package com.parkvision.iam.domain.commands;

public record CreatePermissionCommand(String name, String resource, String action, String description) {}
