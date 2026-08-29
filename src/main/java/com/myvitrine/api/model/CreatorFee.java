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
 * Valor a ser pago a um criador de conteudo por uma Hiring concluida
 * (relacao 1:1). Nomeado CreatorFee (em vez de "Cache") para nao confundir
 * com cache de memoria.
 */
@Entity
@Table(name = "creator_fees")
public class CreatorFee {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiring_id", nullable = false, unique = true)
    private Hiring hiring;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "platform_retention_amount", nullable = false)
    private BigDecimal platformRetentionAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    protected CreatorFee() {
        // JPA
    }

    public CreatorFee(UUID id, Hiring hiring, BigDecimal amount,
                       BigDecimal platformRetentionAmount, PaymentStatus status) {
        this.id = id;
        this.hiring = hiring;
        this.amount = amount;
        this.platformRetentionAmount = platformRetentionAmount;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public Hiring getHiring() {
        return hiring;
    }

    public BigDecimal getAmount() {
        return amount;
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
        if (!(o instanceof CreatorFee that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
