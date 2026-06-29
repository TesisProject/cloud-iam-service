package com.parkvision.iam.domain.commands;

public record UpdateRatingCommand(Long userId, Long zoneId, Short stars, String comment, String type) {}
