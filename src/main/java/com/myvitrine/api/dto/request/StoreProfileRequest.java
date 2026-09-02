package com.myvitrine.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StoreProfileRequest(
        UUID userId,
        @NotBlank String storeName,
        String description
) {
}
