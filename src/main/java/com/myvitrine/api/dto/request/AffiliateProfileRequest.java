package com.myvitrine.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AffiliateProfileRequest(
        String bio,
        String niche
) {
}
