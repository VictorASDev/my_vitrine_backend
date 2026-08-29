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
 * Perfil complementar de um usuario do tipo CREATOR.
 * Compartilha a chave primaria com User (relacao 1:1 opcional).
 */
@Entity
@Table(name = "creator_profiles")
public class CreatorProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "bio")
    private String bio;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    protected CreatorProfile() {
        // JPA
    }

    public CreatorProfile(User user, String bio, String portfolioUrl) {
        this.user = user;
        this.bio = bio;
        this.portfolioUrl = portfolioUrl;
    }

    public UUID getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPortfolioUrl() {
        return portfolioUrl;
    }

    public void setPortfolioUrl(String portfolioUrl) {
        this.portfolioUrl = portfolioUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreatorProfile that)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
