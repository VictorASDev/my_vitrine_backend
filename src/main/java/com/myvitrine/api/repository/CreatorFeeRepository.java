package com.myvitrine.api.repository;

import com.myvitrine.api.model.CreatorFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreatorFeeRepository extends JpaRepository<CreatorFee, UUID> {

    Optional<CreatorFee> findByHiringId(UUID hiringId);

    List<CreatorFee> findByHiringCreatorUserId(UUID creatorId);
}
