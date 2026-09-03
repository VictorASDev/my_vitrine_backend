package com.myvitrine.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myvitrine.api.model.CreatorFee;

@Repository
public interface CreatorFeeRepository extends JpaRepository<CreatorFee, UUID> {

    Optional<CreatorFee> findByHiringId(UUID hiringId);

    List<CreatorFee> findByHiringCreatorUserId(UUID creatorId);

    Page<CreatorFee> findByHiringCreatorUserId(UUID creatorId, Pageable pageable);
}
