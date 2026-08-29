package com.myvitrine.api.repository;

import com.myvitrine.api.model.StoreProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StoreProfileRepository extends JpaRepository<StoreProfile, UUID> {
}
