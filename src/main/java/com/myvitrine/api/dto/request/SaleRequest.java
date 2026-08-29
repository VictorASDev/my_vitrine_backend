package com.myvitrine.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SaleRequest(
        @NotBlank String affiliateLinkCode,
        @NotNull @Positive BigDecimal amount
) {
}
