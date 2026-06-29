package com.parkvision.iam.domain.commands;

public record RemovePermissionFromRoleCommand(Long roleId, Long permissionId) {}
