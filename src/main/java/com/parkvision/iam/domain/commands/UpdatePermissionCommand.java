package com.parkvision.iam.domain.commands;

public record UpdatePermissionCommand(Long permissionId, String name, String resource, String action, String description) {}
