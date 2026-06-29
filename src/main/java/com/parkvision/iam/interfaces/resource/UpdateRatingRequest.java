package com.parkvision.iam.interfaces.resource;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateRatingRequest(
        @NotNull @Min(1) @Max(5) Short stars,
        String comment,
        @Size(max = 30) String type
) {}
