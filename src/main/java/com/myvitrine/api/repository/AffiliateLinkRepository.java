package com.myvitrine.api.repository;

import com.myvitrine.api.model.AffiliateLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AffiliateLinkRepository extends JpaRepository<AffiliateLink, UUID> {

    Optional<AffiliateLink> findByCode(String code);

    boolean existsByCode(String code);

    List<AffiliateLink> findByAffiliateUserId(UUID affiliateId);

    List<AffiliateLink> findByProductId(UUID productId);
}
