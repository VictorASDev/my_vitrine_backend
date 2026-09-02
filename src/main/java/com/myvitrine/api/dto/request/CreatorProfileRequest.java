package com.myvitrine.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;
import java.util.List;

public record CreatorProfileRequest(
        UUID userId,
        String bio,
        String niche,
        List<SocialNetworkRequest> socialNetworks,
        String profilePhotoUrl
) {
}
