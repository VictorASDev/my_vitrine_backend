package com.myvitrine.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Venda rastreada a partir de um AffiliateLink.
 */
@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliate_link_id", nullable = false)
    private AffiliateLink affiliateLink;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    protected Sale() {
        // JPA
    }

    public Sale(UUID id, AffiliateLink affiliateLink, BigDecimal amount, LocalDateTime saleDate) {
        this.id = id;
        this.affiliateLink = affiliateLink;
        this.amount = amount;
        this.saleDate = saleDate;
    }

    public UUID getId() {
        return id;
    }

    public AffiliateLink getAffiliateLink() {
        return affiliateLink;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sale sale)) return false;
        return Objects.equals(id, sale.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
