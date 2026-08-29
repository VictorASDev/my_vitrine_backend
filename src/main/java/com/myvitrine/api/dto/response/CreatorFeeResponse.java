package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.CreatorFee;
import com.myvitrine.api.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatorFeeResponse(
        UUID id,
        UUID hiringId,
        BigDecimal amount,
        BigDecimal platformRetentionAmount,
        PaymentStatus status
) {
    public static CreatorFeeResponse from(CreatorFee fee) {
        return new CreatorFeeResponse(
                fee.getId(),
                fee.getHiring().getId(),
                fee.getAmount(),
                fee.getPlatformRetentionAmount(),
                fee.getStatus());
    }
}
