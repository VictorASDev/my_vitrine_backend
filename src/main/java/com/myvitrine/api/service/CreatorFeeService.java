package com.myvitrine.api.service;

import com.myvitrine.api.dto.response.CreatorFeeResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.CreatorFee;
import com.myvitrine.api.model.Hiring;
import com.myvitrine.api.model.enums.HiringStatus;
import com.myvitrine.api.model.enums.PaymentStatus;
import com.myvitrine.api.repository.CreatorFeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class CreatorFeeService {

    /** Retencao de referencia da plataforma sobre o cache do criador. */
    private static final BigDecimal PLATFORM_RETENTION_PERCENTAGE = new BigDecimal("20.00");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final CreatorFeeRepository creatorFeeRepository;

    public CreatorFeeService(CreatorFeeRepository creatorFeeRepository) {
        this.creatorFeeRepository = creatorFeeRepository;
    }

    /**
     * Gera o CreatorFee de uma Hiring recem-criada, com status PENDING,
     * aplicando a retencao de referencia da plataforma sobre o valor do
     * cache.
     */
    @Transactional
    public CreatorFeeResponse generateForHiring(Hiring hiring, BigDecimal amount) {
        BigDecimal platformRetentionAmount = amount
                .multiply(PLATFORM_RETENTION_PERCENTAGE)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        CreatorFee fee = new CreatorFee(UUID.randomUUID(), hiring, amount, platformRetentionAmount, PaymentStatus.PENDING);
        return CreatorFeeResponse.from(creatorFeeRepository.save(fee));
    }

    public CreatorFeeResponse findById(UUID id) {
        return CreatorFeeResponse.from(getCreatorFeeOrThrow(id));
    }

    public List<CreatorFeeResponse> findAll() {
        return creatorFeeRepository.findAll().stream().map(CreatorFeeResponse::from).toList();
    }

    public Page<CreatorFeeResponse> findAll(Pageable pageable) {
        return creatorFeeRepository.findAll(pageable).map(CreatorFeeResponse::from);
    }

    public List<CreatorFeeResponse> findByCreator(UUID creatorId) {
        return creatorFeeRepository.findByHiringCreatorUserId(creatorId).stream()
                .map(CreatorFeeResponse::from).toList();
    }

    public Page<CreatorFeeResponse> findByCreator(UUID creatorId, Pageable pageable) {
        return creatorFeeRepository.findByHiringCreatorUserId(creatorId, pageable).map(CreatorFeeResponse::from);
    }

    /**
     * Confirma o pagamento do cache. Regra de negocio: so pode ser
     * confirmado quando a contratacao ja foi aprovada (APPROVED).
     */
    @Transactional
    public CreatorFeeResponse confirm(UUID id) {
        CreatorFee fee = getCreatorFeeOrThrow(id);
        if (fee.getStatus() == PaymentStatus.CONFIRMED) {
            throw new BusinessRuleException("Cache " + id + " ja esta confirmado");
        }
        if (fee.getHiring().getStatus() != HiringStatus.APPROVED) {
            throw new BusinessRuleException("O cache so pode ser confirmado apos a contratacao ser aprovada (APPROVED)");
        }
        fee.setStatus(PaymentStatus.CONFIRMED);
        return CreatorFeeResponse.from(fee);
    }

    CreatorFee getCreatorFeeOrThrow(UUID id) {
        return creatorFeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cache de criador nao encontrado: " + id));
    }
}
