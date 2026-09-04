package com.myvitrine.api.model;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Perfil complementar de um usuario do tipo AFFILIATE.
 * Compartilha a chave primaria com User (relacao 1:1 opcional).
 */
@Entity
@Table(name = "affiliate_profiles")
public class AffiliateProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "bio")
    private String bio;

    @Column(name = "niche")
    private String niche;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    protected AffiliateProfile() {
        // JPA
    }

    public AffiliateProfile(User user, String bio, String niche, String profilePhotoUrl) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
        this.bio = bio;
        this.niche = niche;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public AffiliateProfile(User user, String bio, String niche) {
        this(user, bio, niche, null);
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

    public String getNiche() {
        return niche;
    }

    public void setNiche(String niche) {
        this.niche = niche;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AffiliateProfile that)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
