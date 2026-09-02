package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.CreatorProfile;

import java.util.UUID;
import java.util.List;

public record CreatorProfileResponse(
        UUID userId,
        String bio,
        String niche,
        List<SocialNetworkResponse> socialNetworks,
        String profilePhotoUrl
) {
    public static CreatorProfileResponse from(CreatorProfile profile, List<SocialNetworkResponse> socialNetworks) {
        return new CreatorProfileResponse(profile.getUserId(), profile.getBio(), profile.getNiche(), socialNetworks,
                profile.getProfilePhotoUrl());
    }
}
