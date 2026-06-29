package com.parkvision.iam.domain.commands;

public record ChangeUserRoleCommand(Long userId, String roleName) {}
