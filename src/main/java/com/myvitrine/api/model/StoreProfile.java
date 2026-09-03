package com.myvitrine.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

/**
 * Perfil complementar de um usuario do tipo STORE.
 * Compartilha a chave primaria com User (relacao 1:1 opcional).
 */
@Entity
@Table(name = "store_profiles")
public class StoreProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "description")
    private String description;

    @Column(name = "niche")
    private String niche;

    @Column(name = "cnpj")
    private String cnpj;

    protected StoreProfile() {
        // JPA
    }

    public StoreProfile(User user, String storeName, String description, String niche, String cnpj) {
        this.userId = user.getId();
        this.user = user;
        this.storeName = storeName;
        this.description = description;
        this.niche = niche;
        this.cnpj = cnpj;
    }

    public StoreProfile(User user, String storeName, String description) {
        this(user, storeName, description, null, null);
    }

    public UUID getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNiche() {
        return niche;
    }

    public void setNiche(String niche) {
        this.niche = niche;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoreProfile that)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
