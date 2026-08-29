package com.myvitrine.api.repository;

import com.myvitrine.api.model.StoreProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreProfileRepository extends JpaRepository<StoreProfile, UUID> {
}
