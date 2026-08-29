package com.myvitrine.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
        @NotNull UUID storeId,
        @NotBlank String name,
        @NotNull @Positive BigDecimal price,
        @PositiveOrZero BigDecimal commissionPercentage,
        String imageUrl
) {
}
