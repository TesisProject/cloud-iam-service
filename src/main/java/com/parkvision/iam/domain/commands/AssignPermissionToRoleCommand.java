package com.parkvision.iam.domain.commands;

public record AssignPermissionToRoleCommand(Long roleId, Long permissionId) {}
