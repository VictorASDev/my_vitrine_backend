package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.enums.HiringStatus;

import java.util.Map;

public record FindTotalHiringsResponse(
        Map<HiringStatus, Long> totals
) {
}
