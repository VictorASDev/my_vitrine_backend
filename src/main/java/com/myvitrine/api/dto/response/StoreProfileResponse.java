package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.StoreProfile;

import java.util.UUID;
import java.util.List;

public record StoreProfileResponse(
        UUID userId,
        String storeName,
        String description,
        String niche,
        String cnpj,
        List<SocialNetworkResponse> socialNetworks
) {
    public static StoreProfileResponse from(StoreProfile profile, List<SocialNetworkResponse> socialNetworks) {
        return new StoreProfileResponse(profile.getUserId(), profile.getStoreName(), profile.getDescription(),
                profile.getNiche(), profile.getCnpj(), socialNetworks);
    }
}
