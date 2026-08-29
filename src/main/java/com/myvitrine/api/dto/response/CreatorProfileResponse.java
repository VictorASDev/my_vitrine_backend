package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.CreatorProfile;

import java.util.UUID;

public record CreatorProfileResponse(
        UUID userId,
        String bio,
        String portfolioUrl
) {
    public static CreatorProfileResponse from(CreatorProfile profile) {
        return new CreatorProfileResponse(profile.getUserId(), profile.getBio(), profile.getPortfolioUrl());
    }
}
