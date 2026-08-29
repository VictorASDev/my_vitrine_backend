package com.myvitrine.api.repository;

import com.myvitrine.api.model.Commission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommissionRepository extends JpaRepository<Commission, UUID> {

    Optional<Commission> findBySaleId(UUID saleId);

    List<Commission> findBySaleAffiliateLinkAffiliateUserId(UUID affiliateId);
}
