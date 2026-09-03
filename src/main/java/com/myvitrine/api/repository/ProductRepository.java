package com.myvitrine.api.repository;

import com.myvitrine.api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByStoreUserId(UUID storeId);

    Page<Product> findByStoreUserId(UUID storeId, Pageable pageable);

    List<Product> findByActiveTrue();
}
