package com.myvitrine.api.model;

import com.myvitrine.api.model.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Comissao de afiliado gerada a partir de uma Sale (relacao 1:1).
 */
@Entity
@Table(name = "commissions")
public class Commission {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false, unique = true)
    private Sale sale;

    @Column(name = "commission_amount", nullable = false)
    private BigDecimal commissionAmount;

    @Column(name = "platform_retention_amount", nullable = false)
    private BigDecimal platformRetentionAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    protected Commission() {
        // JPA
    }

    public Commission(UUID id, Sale sale, BigDecimal commissionAmount,
                       BigDecimal platformRetentionAmount, PaymentStatus status) {
        this.id = id;
        this.sale = sale;
        this.commissionAmount = commissionAmount;
        this.platformRetentionAmount = platformRetentionAmount;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public Sale getSale() {
        return sale;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public BigDecimal getPlatformRetentionAmount() {
        return platformRetentionAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Commission that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
