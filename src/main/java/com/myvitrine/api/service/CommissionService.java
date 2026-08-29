package com.myvitrine.api.service;

import com.myvitrine.api.dto.response.CommissionResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.Commission;
import com.myvitrine.api.model.Sale;
import com.myvitrine.api.model.enums.PaymentStatus;
import com.myvitrine.api.repository.CommissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class CommissionService {

    /** Retencao de referencia da plataforma sobre o valor da comissao. */
    private static final BigDecimal PLATFORM_RETENTION_PERCENTAGE = new BigDecimal("20.00");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final CommissionRepository commissionRepository;

    public CommissionService(CommissionRepository commissionRepository) {
        this.commissionRepository = commissionRepository;
    }

    /**
     * Calcula a comissao do afiliado (percentual do produto sobre o valor
     * da venda) e a retencao da plataforma (percentual de referencia sobre
     * a comissao), criando a Commission com status PENDING.
     */
    @Transactional
    public CommissionResponse generateForSale(Sale sale) {
        BigDecimal commissionPercentage = sale.getAffiliateLink().getProduct().getCommissionPercentage();
        BigDecimal commissionAmount = sale.getAmount()
                .multiply(commissionPercentage)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal platformRetentionAmount = commissionAmount
                .multiply(PLATFORM_RETENTION_PERCENTAGE)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        Commission commission = new Commission(UUID.randomUUID(), sale, commissionAmount,
                platformRetentionAmount, PaymentStatus.PENDING);
        return CommissionResponse.from(commissionRepository.save(commission));
    }

    public CommissionResponse findById(UUID id) {
        return CommissionResponse.from(getCommissionOrThrow(id));
    }

    public List<CommissionResponse> findAll() {
        return commissionRepository.findAll().stream().map(CommissionResponse::from).toList();
    }

    public List<CommissionResponse> findByAffiliate(UUID affiliateId) {
        return commissionRepository.findBySaleAffiliateLinkAffiliateUserId(affiliateId).stream()
                .map(CommissionResponse::from).toList();
    }

    @Transactional
    public CommissionResponse confirm(UUID id) {
        Commission commission = getCommissionOrThrow(id);
        if (commission.getStatus() == PaymentStatus.CONFIRMED) {
            throw new BusinessRuleException("Comissao " + id + " ja esta confirmada");
        }
        commission.setStatus(PaymentStatus.CONFIRMED);
        return CommissionResponse.from(commission);
    }

    Commission getCommissionOrThrow(UUID id) {
        return commissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comissao nao encontrada: " + id));
    }
}
