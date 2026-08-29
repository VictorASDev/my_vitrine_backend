package com.myvitrine.api.repository;

import com.myvitrine.api.model.Hiring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HiringRepository extends JpaRepository<Hiring, UUID> {

    List<Hiring> findByStoreUserId(UUID storeId);

    List<Hiring> findByCreatorUserId(UUID creatorId);
}
