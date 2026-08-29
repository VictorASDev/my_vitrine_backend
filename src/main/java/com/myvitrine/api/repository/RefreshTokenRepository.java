package com.myvitrine.api.repository;

import com.myvitrine.api.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenId(UUID tokenId);

    List<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);
}
