package com.myvitrine.api.model;

import com.myvitrine.api.model.enums.AffiliateLinkType;
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
 * Link ou cupom gerado por um afiliado para divulgar um produto.
 */
@Entity
@Table(name = "affiliate_links")
public class AffiliateLink {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_id", nullable = false)
    private AffiliateProfile affiliate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AffiliateLinkType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AffiliateLink() {
        // JPA
    }

    public AffiliateLink(UUID id, AffiliateProfile affiliate, Product product, String code,
                          AffiliateLinkType type, LocalDateTime createdAt) {
        this.id = id;
        this.affiliate = affiliate;
        this.product = product;
        this.code = code;
        this.type = type;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public AffiliateProfile getAffiliate() {
        return affiliate;
    }

    public Product getProduct() {
        return product;
    }

    public String getCode() {
        return code;
    }

    public AffiliateLinkType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AffiliateLink that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
