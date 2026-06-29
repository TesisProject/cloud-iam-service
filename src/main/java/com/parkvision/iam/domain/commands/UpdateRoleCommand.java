package com.parkvision.iam.domain.commands;

public record UpdateRoleCommand(Long roleId, String name, String description) {}
