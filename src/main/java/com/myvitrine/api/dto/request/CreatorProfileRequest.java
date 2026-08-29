package com.myvitrine.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatorProfileRequest(
        @NotNull UUID userId,
        String bio,
        String portfolioUrl
) {
}
