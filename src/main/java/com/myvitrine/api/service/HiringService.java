package com.myvitrine.api.service;

import com.myvitrine.api.dto.projection.HiringStatusCountProjection;
import com.myvitrine.api.dto.request.HiringRequest;
import com.myvitrine.api.dto.response.CreatorDashboardResponse;
import com.myvitrine.api.dto.response.CreatorFeeResponse;
import com.myvitrine.api.dto.response.FindTotalHiringsResponse;
import com.myvitrine.api.dto.response.HiringResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.CreatorProfile;
import com.myvitrine.api.model.Hiring;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.enums.HiringStatus;
import com.myvitrine.api.repository.HiringRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HiringService {

    /** Valor de referencia do cache por video quando o lojista nao informa um valor especifico. */
    static final BigDecimal DEFAULT_FEE_AMOUNT = new BigDecimal("250.00");

    /** Transicoes de status validas: REQUESTED -> ACCEPTED -> IN_PRODUCTION -> DELIVERED -> APPROVED. */
    private static final Map<HiringStatus, Set<HiringStatus>> VALID_TRANSITIONS = new EnumMap<>(Map.of(
            HiringStatus.REQUESTED, EnumSet.of(HiringStatus.REJECTED, HiringStatus.ACCEPTED),
            HiringStatus.REJECTED, EnumSet.noneOf(HiringStatus.class),
            HiringStatus.ACCEPTED, EnumSet.of(HiringStatus.IN_PRODUCTION),
            HiringStatus.IN_PRODUCTION, EnumSet.of(HiringStatus.DELIVERED),
            HiringStatus.DELIVERED, EnumSet.of(HiringStatus.APPROVED),
            HiringStatus.APPROVED, EnumSet.noneOf(HiringStatus.class)
    ));

    private final HiringRepository hiringRepository;
    private final StoreProfileService storeProfileService;
    private final CreatorProfileService creatorProfileService;
    private final ProductService productService;
    private final CreatorFeeService creatorFeeService;

    public HiringService(HiringRepository hiringRepository, StoreProfileService storeProfileService,
                          CreatorProfileService creatorProfileService, ProductService productService,
                          CreatorFeeService creatorFeeService) {
        this.hiringRepository = hiringRepository;
        this.storeProfileService = storeProfileService;
        this.creatorProfileService = creatorProfileService;
        this.productService = productService;
        this.creatorFeeService = creatorFeeService;
    }

    /**
     * Cria a contratacao com status REQUESTED e ja gera o CreatorFee
     * correspondente (valor de referencia ou informado pelo lojista),
     * com status PENDING ate a aprovacao do conteudo.
     */
    @Transactional
    public HiringResponse create(HiringRequest request) {
        StoreProfile store = storeProfileService.getStoreProfileOrThrow(request.storeId());
        CreatorProfile creator = creatorProfileService.getCreatorProfileOrThrow(request.creatorId());
        Product product = productService.getProductOrThrow(request.productId());

        Hiring hiring = new Hiring(UUID.randomUUID(), store, creator, product,
                HiringStatus.REQUESTED, LocalDateTime.now());
        Hiring saved = hiringRepository.save(hiring);

        BigDecimal feeAmount = request.feeAmount() != null ? request.feeAmount() : DEFAULT_FEE_AMOUNT;
        creatorFeeService.generateForHiring(saved, feeAmount);

        return HiringResponse.from(saved);
    }

    public HiringResponse findById(UUID id) {
        return HiringResponse.from(getHiringOrThrow(id));
    }

    public List<HiringResponse> findAll() {
        return hiringRepository.findAll().stream().map(HiringResponse::from).toList();
    }

    public Page<HiringResponse> findAll(Pageable pageable) {
        return hiringRepository.findAll(pageable).map(HiringResponse::from);
    }

    public List<HiringResponse> findByStore(UUID storeId) {
        return hiringRepository.findByStoreUserId(storeId).stream().map(HiringResponse::from).toList();
    }

    public Page<HiringResponse> findByStore(UUID storeId, Pageable pageable) {
        return hiringRepository.findByStoreUserId(storeId, pageable).map(HiringResponse::from);
    }

    public List<HiringResponse> findByCreator(UUID creatorId) {
        return hiringRepository.findByCreatorUserId(creatorId).stream().map(HiringResponse::from).toList();
    }

    public Page<HiringResponse> findByCreator(UUID creatorId, Pageable pageable) {
        return hiringRepository.findByCreatorUserId(creatorId, pageable).map(HiringResponse::from);
    }

    public CreatorDashboardResponse getCreatorDashboard(UUID creatorId) {
        List<Hiring> hirings = hiringRepository.findByCreatorUserId(creatorId);
        List<CreatorFeeResponse> creatorFees = creatorFeeService.findByCreator(creatorId);

        List<HiringResponse> recentJobs = hirings.stream()
                .sorted(Comparator.comparing(Hiring::getCreatedAt).reversed())
                .limit(5)
                .map(HiringResponse::from)
                .toList();

        long totalJobs = hirings.size();
        long pendingProposals = hirings.stream().filter(h -> h.getStatus() == HiringStatus.REQUESTED).count();
        long activeJobs = hirings.stream()
                .filter(h -> h.getStatus() == HiringStatus.REQUESTED
                        || h.getStatus() == HiringStatus.ACCEPTED
                        || h.getStatus() == HiringStatus.IN_PRODUCTION
                        || h.getStatus() == HiringStatus.DELIVERED)
                .count();
        long completedJobs = hirings.stream().filter(h -> h.getStatus() == HiringStatus.APPROVED).count();

        BigDecimal totalFees = creatorFees.stream()
                .map(CreatorFeeResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingFees = creatorFees.stream()
                .filter(f -> f.status() == com.myvitrine.api.model.enums.PaymentStatus.PENDING)
                .map(CreatorFeeResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal approvedFees = creatorFees.stream()
                .filter(f -> f.status() == com.myvitrine.api.model.enums.PaymentStatus.CONFIRMED)
                .map(CreatorFeeResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CreatorDashboardResponse(
                totalJobs,
                pendingProposals,
                activeJobs,
                completedJobs,
                totalFees,
                pendingFees,
                approvedFees,
                recentJobs
        );
    }

    public FindTotalHiringsResponse findTotalByCreator(UUID creatorId) {
        List<HiringStatusCountProjection> results =
                hiringRepository.countByCreatorIdGroupedByStatus(creatorId);

        Map<HiringStatus, Long> totals = results.stream()
                .collect(Collectors.toMap(
                        HiringStatusCountProjection::getStatus,
                        HiringStatusCountProjection::getTotal
                ));

        Arrays.stream(HiringStatus.values())
                .forEach(status -> totals.putIfAbsent(status, 0L));

        return new FindTotalHiringsResponse(totals);
    }

    /**
     * Avanca o status da contratacao, validando que a transicao pedida e
     * permitida pela maquina de estados do dominio.
     */
    @Transactional
    public HiringResponse updateStatus(UUID id, HiringStatus newStatus) {
        Hiring hiring = getHiringOrThrow(id);
        Set<HiringStatus> allowed = VALID_TRANSITIONS.get(hiring.getStatus());
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new BusinessRuleException("Transicao de status invalida: " + hiring.getStatus() + " -> " + newStatus);
        }
        hiring.setStatus(newStatus);
        return HiringResponse.from(hiring);
    }

    Hiring getHiringOrThrow(UUID id) {
        return hiringRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contratacao nao encontrada: " + id));
    }
}
