package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.Sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SaleResponse(
        UUID id,
        UUID affiliateLinkId,
        String affiliateLinkCode,
        BigDecimal amount,
        LocalDateTime saleDate
) {
    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getAffiliateLink().getId(),
                sale.getAffiliateLink().getCode(),
                sale.getAmount(),
                sale.getSaleDate());
    }
}
