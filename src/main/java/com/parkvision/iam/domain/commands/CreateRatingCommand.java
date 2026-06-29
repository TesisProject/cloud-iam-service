package com.parkvision.iam.domain.commands;

public record CreateRatingCommand(Long userId, Long zoneId, Short stars, String comment, String type) {}
