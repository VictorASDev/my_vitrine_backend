package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.Hiring;
import com.myvitrine.api.model.enums.HiringStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record HiringResponse(
        UUID id,
        UUID storeId,
        String storeName,
        UUID creatorId,
        UUID productId,
        String productName,
        HiringStatus status,
        LocalDateTime createdAt
) {
    public static HiringResponse from(Hiring hiring) {
        return new HiringResponse(
                hiring.getId(),
                hiring.getStore().getUserId(),
                hiring.getStore().getStoreName(),
                hiring.getCreator().getUserId(),
                hiring.getProduct().getId(),
                hiring.getProduct().getName(),
                hiring.getStatus(),
                hiring.getCreatedAt());
    }
}
