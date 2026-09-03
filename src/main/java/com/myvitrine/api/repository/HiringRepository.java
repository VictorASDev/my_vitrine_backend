package com.myvitrine.api.repository;

import com.myvitrine.api.model.Hiring;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HiringRepository extends JpaRepository<Hiring, UUID> {

    List<Hiring> findByStoreUserId(UUID storeId);

    Page<Hiring> findByStoreUserId(UUID storeId, Pageable pageable);

    List<Hiring> findByCreatorUserId(UUID creatorId);

    Page<Hiring> findByCreatorUserId(UUID creatorId, Pageable pageable);
}
