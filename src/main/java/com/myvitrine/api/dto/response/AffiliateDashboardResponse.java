package com.myvitrine.api.dto.response;

import java.math.BigDecimal;

public record AffiliateDashboardResponse(
        long totalLinks,
        long totalSales,
        BigDecimal totalSalesAmount,
        BigDecimal totalCommissions,
        BigDecimal pendingCommissions,
        BigDecimal confirmedCommissions
) {
}
