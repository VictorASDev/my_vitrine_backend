package com.myvitrine.api.repository;

import com.myvitrine.api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    @EntityGraph(attributePaths = {"store"})
    List<Product> findByStoreUserId(UUID storeId);

    @EntityGraph(attributePaths = {"store"})
    Page<Product> findByStoreUserId(UUID storeId, Pageable pageable);

    @EntityGraph(attributePaths = {"store"})
    List<Product> findByActiveTrue();

    @EntityGraph(attributePaths = {"store"})
    @Override
    java.util.Optional<Product> findById(UUID id);
}

