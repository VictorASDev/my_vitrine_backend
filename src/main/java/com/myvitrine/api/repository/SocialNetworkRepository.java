package com.myvitrine.api.repository;

import com.myvitrine.api.model.SocialNetwork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SocialNetworkRepository extends JpaRepository<SocialNetwork, UUID> {
    List<SocialNetwork> findAllByUser_Id(UUID userId);

    void deleteAllByUser_Id(UUID userId);
}