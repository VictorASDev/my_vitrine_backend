package com.myvitrine.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatorProfileRequest(
        String bio,
        String portfolioUrl
) {
}
