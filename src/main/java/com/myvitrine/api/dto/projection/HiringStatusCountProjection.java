package com.myvitrine.api.dto.projection;

import com.myvitrine.api.model.enums.HiringStatus;

public interface HiringStatusCountProjection {

    HiringStatus getStatus();

    long getTotal();
}