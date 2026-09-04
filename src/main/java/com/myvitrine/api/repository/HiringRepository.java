package com.myvitrine.api.repository;

import com.myvitrine.api.dto.projection.HiringStatusCountProjection;
import com.myvitrine.api.model.Hiring;
import com.myvitrine.api.model.enums.HiringStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HiringRepository extends JpaRepository<Hiring, UUID> {

    @EntityGraph(attributePaths = {
            "store",
            "creator",
            "product"
    })
    @Override
    Optional<Hiring> findById(UUID id);

    @EntityGraph(attributePaths = {
            "store",
            "creator",
            "product"
    })
    List<Hiring> findByStoreUserId(UUID storeId);

    @EntityGraph(attributePaths = {
            "store",
            "creator",
            "product"
    })
    Page<Hiring> findByStoreUserId(UUID storeId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "store",
            "creator",
            "product"
    })
    List<Hiring> findByCreatorUserId(UUID creatorId);

    @EntityGraph(attributePaths = {
            "store",
            "creator",
            "product"
    })
    Page<Hiring> findByCreatorUserId(UUID creatorId, Pageable pageable);


    //Busca o valor total de contratações status
    @Query("""
        SELECT h.status AS status, COUNT(h) AS total
        FROM Hiring h
        WHERE h.creator.userId = :userId
        GROUP BY h.status
    """)
    @EntityGraph(attributePaths = {
            "store",
            "creator",
            "product"
    })
    List<HiringStatusCountProjection> countByCreatorIdGroupedByStatus(
            @Param("userId") UUID userId
    );

}
