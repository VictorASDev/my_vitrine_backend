package com.myvitrine.api.repository;

import com.myvitrine.api.model.AffiliateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AffiliateProfileRepository extends JpaRepository<AffiliateProfile, UUID> {
}
