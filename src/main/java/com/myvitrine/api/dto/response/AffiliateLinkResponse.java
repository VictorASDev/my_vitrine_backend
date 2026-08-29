package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.AffiliateLink;
import com.myvitrine.api.model.enums.AffiliateLinkType;

import java.time.LocalDateTime;
import java.util.UUID;

public record AffiliateLinkResponse(
        UUID id,
        UUID affiliateId,
        UUID productId,
        String productName,
        String code,
        AffiliateLinkType type,
        LocalDateTime createdAt
) {
    public static AffiliateLinkResponse from(AffiliateLink link) {
        return new AffiliateLinkResponse(
                link.getId(),
                link.getAffiliate().getUserId(),
                link.getProduct().getId(),
                link.getProduct().getName(),
                link.getCode(),
                link.getType(),
                link.getCreatedAt());
    }
}
