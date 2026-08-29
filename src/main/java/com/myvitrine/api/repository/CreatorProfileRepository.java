package com.myvitrine.api.repository;

import com.myvitrine.api.model.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, UUID> {
}
