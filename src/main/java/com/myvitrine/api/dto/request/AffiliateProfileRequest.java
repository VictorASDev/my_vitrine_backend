package com.myvitrine.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AffiliateProfileRequest(
        @NotNull UUID userId,
        String bio,
        String niche
) {
}
