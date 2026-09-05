package com.myvitrine.api.dto.response;

import java.math.BigDecimal;

public record StoreDashboardResponse(
        long totalProducts,
        long activeProducts,
        long inactiveProducts,
        long totalSales,
        BigDecimal totalSalesAmount,
        long totalHirings,
        long pendingHirings,
        long activeHirings,
        long completedHirings
) {
}
