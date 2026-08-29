package com.myvitrine.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record HiringRequest(
        @NotNull UUID storeId,
        @NotNull UUID creatorId,
        @NotNull UUID productId,
        @PositiveOrZero BigDecimal feeAmount
) {
}
