package com.myvitrine.api.repository;

import com.myvitrine.api.model.AffiliateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AffiliateProfileRepository extends JpaRepository<AffiliateProfile, UUID> {
}
