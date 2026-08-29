package com.myvitrine.api.repository;

import com.myvitrine.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByStoreUserId(UUID storeId);

    List<Product> findByActiveTrue();
}
