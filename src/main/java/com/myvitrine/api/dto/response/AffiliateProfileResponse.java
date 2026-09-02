package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.AffiliateProfile;

import java.util.UUID;
import java.util.List;

public record AffiliateProfileResponse(
        UUID userId,
        String bio,
        String niche,
        List<SocialNetworkResponse> socialNetworks,
        String profilePhotoUrl
) {
    public static AffiliateProfileResponse from(AffiliateProfile profile, List<SocialNetworkResponse> socialNetworks) {
        return new AffiliateProfileResponse(profile.getUserId(), profile.getBio(), profile.getNiche(), socialNetworks,
                profile.getProfilePhotoUrl());
    }
}
