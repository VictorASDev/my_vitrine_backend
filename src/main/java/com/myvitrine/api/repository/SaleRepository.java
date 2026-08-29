package com.myvitrine.api.repository;

import com.myvitrine.api.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    List<Sale> findByAffiliateLinkId(UUID affiliateLinkId);
}
