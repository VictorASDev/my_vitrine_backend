package com.myvitrine.api.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CreatorDashboardResponse(
        long totalJobs,
        long pendingProposals,
        long activeJobs,
        long completedJobs,
        BigDecimal totalFees,
        BigDecimal pendingFees,
        BigDecimal approvedFees,
        List<HiringResponse> recentJobs
) {
}
