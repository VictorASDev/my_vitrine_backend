package com.myvitrine.api.repository;

import com.myvitrine.api.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    List<Sale> findByAffiliateLinkId(UUID affiliateLinkId);

    Page<Sale> findByAffiliateLinkId(UUID affiliateLinkId, Pageable pageable);
}
