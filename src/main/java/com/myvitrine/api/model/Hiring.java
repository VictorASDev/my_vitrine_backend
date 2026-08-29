package com.myvitrine.api.model;

import com.myvitrine.api.model.enums.HiringStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Contratacao de um criador de conteudo por um lojista para produzir
 * conteudo sobre um produto especifico.
 */
@Entity
@Table(name = "hirings")
public class Hiring {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreProfile store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private CreatorProfile creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HiringStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Hiring() {
        // JPA
    }

    public Hiring(UUID id, StoreProfile store, CreatorProfile creator, Product product,
                   HiringStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.store = store;
        this.creator = creator;
        this.product = product;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public StoreProfile getStore() {
        return store;
    }

    public CreatorProfile getCreator() {
        return creator;
    }

    public Product getProduct() {
        return product;
    }

    public HiringStatus getStatus() {
        return status;
    }

    public void setStatus(HiringStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hiring hiring)) return false;
        return Objects.equals(id, hiring.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
