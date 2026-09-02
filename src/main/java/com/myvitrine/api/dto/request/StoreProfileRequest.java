package com.myvitrine.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;
import java.util.List;

public record StoreProfileRequest(
        UUID userId,
        @NotBlank String storeName,
        String description,
        String niche,
        String cnpj,
        List<SocialNetworkRequest> socialNetworks
) {
}
