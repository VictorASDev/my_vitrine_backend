package com.myvitrine.api.dto.request;

import com.myvitrine.api.model.enums.AffiliateLinkType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AffiliateLinkRequest(
        @NotNull UUID affiliateId,
        @NotNull UUID productId,
        @NotNull AffiliateLinkType type
) {
}
