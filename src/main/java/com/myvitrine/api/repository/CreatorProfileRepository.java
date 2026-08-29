package com.myvitrine.api.repository;

import com.myvitrine.api.model.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, UUID> {
}
