package com.myvitrine.api.repository;

import com.myvitrine.api.model.Commission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, UUID> {

    Optional<Commission> findBySaleId(UUID saleId);

    List<Commission> findBySaleAffiliateLinkAffiliateUserId(UUID affiliateId);

    Page<Commission> findBySaleAffiliateLinkAffiliateUserId(UUID affiliateId, Pageable pageable);
}
