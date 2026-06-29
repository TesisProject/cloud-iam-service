package com.parkvision.iam.application.commandservices;

import com.parkvision.iam.domain.model.aggregates.User;

public record AuthResult(User user, String token) {}
