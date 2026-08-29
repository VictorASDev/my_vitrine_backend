package com.myvitrine.api.repository;

import com.myvitrine.api.model.CreatorFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreatorFeeRepository extends JpaRepository<CreatorFee, UUID> {

    Optional<CreatorFee> findByHiringId(UUID hiringId);

    List<CreatorFee> findByHiringCreatorUserId(UUID creatorId);
}
