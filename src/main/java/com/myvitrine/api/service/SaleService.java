package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.SaleRequest;
import com.myvitrine.api.dto.response.SaleResponse;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.AffiliateLink;
import com.myvitrine.api.model.Sale;
import com.myvitrine.api.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final AffiliateLinkService affiliateLinkService;
    private final CommissionService commissionService;

    public SaleService(SaleRepository saleRepository, AffiliateLinkService affiliateLinkService,
                        CommissionService commissionService) {
        this.saleRepository = saleRepository;
        this.affiliateLinkService = affiliateLinkService;
        this.commissionService = commissionService;
    }

    /**
     * Registra uma venda rastreada por um link/cupom de afiliado e, na
     * sequencia, gera automaticamente a Commission correspondente
     * (calculo de comissao e retencao da plataforma centralizado em
     * CommissionService).
     */
    @Transactional
    public SaleResponse create(SaleRequest request) {
        AffiliateLink affiliateLink = affiliateLinkService.getAffiliateLinkByCodeOrThrow(request.affiliateLinkCode());
        Sale sale = new Sale(UUID.randomUUID(), affiliateLink, request.amount(), LocalDateTime.now());
        Sale saved = saleRepository.save(sale);
        commissionService.generateForSale(saved);
        return SaleResponse.from(saved);
    }

    public SaleResponse findById(UUID id) {
        return SaleResponse.from(getSaleOrThrow(id));
    }

    public List<SaleResponse> findAll() {
        return saleRepository.findAll().stream().map(SaleResponse::from).toList();
    }

    public List<SaleResponse> findByAffiliateLink(UUID affiliateLinkId) {
        return saleRepository.findByAffiliateLinkId(affiliateLinkId).stream().map(SaleResponse::from).toList();
    }

    Sale getSaleOrThrow(UUID id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda nao encontrada: " + id));
    }
}
