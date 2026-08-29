package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID storeId,
        String storeName,
        String name,
        BigDecimal price,
        BigDecimal commissionPercentage,
        String imageUrl,
        boolean active,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getStore().getUserId(),
                product.getStore().getStoreName(),
                product.getName(),
                product.getPrice(),
                product.getCommissionPercentage(),
                product.getImageUrl(),
                product.isActive(),
                product.getCreatedAt());
    }
}
