package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.Commission;
import com.myvitrine.api.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CommissionResponse(
        UUID id,
        UUID saleId,
        BigDecimal commissionAmount,
        BigDecimal platformRetentionAmount,
        PaymentStatus status
) {
    public static CommissionResponse from(Commission commission) {
        return new CommissionResponse(
                commission.getId(),
                commission.getSale().getId(),
                commission.getCommissionAmount(),
                commission.getPlatformRetentionAmount(),
                commission.getStatus());
    }
}
