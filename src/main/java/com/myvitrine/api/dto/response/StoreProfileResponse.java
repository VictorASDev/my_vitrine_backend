package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.StoreProfile;

import java.util.UUID;

public record StoreProfileResponse(
        UUID userId,
        String storeName,
        String description
) {
    public static StoreProfileResponse from(StoreProfile profile) {
        return new StoreProfileResponse(profile.getUserId(), profile.getStoreName(), profile.getDescription());
    }
}
