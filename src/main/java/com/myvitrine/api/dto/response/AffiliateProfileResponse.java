package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.AffiliateProfile;

import java.util.UUID;

public record AffiliateProfileResponse(
        UUID userId,
        String bio,
        String niche
) {
    public static AffiliateProfileResponse from(AffiliateProfile profile) {
        return new AffiliateProfileResponse(profile.getUserId(), profile.getBio(), profile.getNiche());
    }
}
