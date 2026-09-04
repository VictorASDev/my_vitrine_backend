package com.myvitrine.api.dto.request;

import com.myvitrine.api.model.enums.HiringStatus;
import jakarta.validation.constraints.NotNull;

public record FindTotalHiringsRequest(@NotNull HiringStatus status) {
}
