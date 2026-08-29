package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.AffiliateLinkRequest;
import com.myvitrine.api.dto.response.AffiliateLinkResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.AffiliateLink;
import com.myvitrine.api.model.AffiliateProfile;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.repository.AffiliateLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AffiliateLinkService {

    private static final String CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AffiliateLinkRepository affiliateLinkRepository;
    private final AffiliateProfileService affiliateProfileService;
    private final ProductService productService;

    public AffiliateLinkService(AffiliateLinkRepository affiliateLinkRepository,
                                 AffiliateProfileService affiliateProfileService,
                                 ProductService productService) {
        this.affiliateLinkRepository = affiliateLinkRepository;
        this.affiliateProfileService = affiliateProfileService;
        this.productService = productService;
    }

    @Transactional
    public AffiliateLinkResponse create(AffiliateLinkRequest request) {
        AffiliateProfile affiliate = affiliateProfileService.getAffiliateProfileOrThrow(request.affiliateId());
        Product product = productService.getProductOrThrow(request.productId());
        if (!product.isActive()) {
            throw new BusinessRuleException("Nao e possivel gerar link/cupom para um produto inativo: " + product.getId());
        }
        AffiliateLink link = new AffiliateLink(UUID.randomUUID(), affiliate, product,
                generateUniqueCode(), request.type(), LocalDateTime.now());
        return AffiliateLinkResponse.from(affiliateLinkRepository.save(link));
    }

    public AffiliateLinkResponse findById(UUID id) {
        return AffiliateLinkResponse.from(getAffiliateLinkOrThrow(id));
    }

    public List<AffiliateLinkResponse> findAll() {
        return affiliateLinkRepository.findAll().stream().map(AffiliateLinkResponse::from).toList();
    }

    public List<AffiliateLinkResponse> findByAffiliate(UUID affiliateId) {
        return affiliateLinkRepository.findByAffiliateUserId(affiliateId).stream()
                .map(AffiliateLinkResponse::from).toList();
    }

    @Transactional
    public void delete(UUID id) {
        AffiliateLink link = getAffiliateLinkOrThrow(id);
        affiliateLinkRepository.delete(link);
    }

    AffiliateLink getAffiliateLinkOrThrow(UUID id) {
        return affiliateLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Link/cupom de afiliado nao encontrado: " + id));
    }

    AffiliateLink getAffiliateLinkByCodeOrThrow(String code) {
        return affiliateLinkRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Link/cupom de afiliado nao encontrado para o codigo: " + code));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = randomCode();
        } while (affiliateLinkRepository.existsByCode(code));
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }
}
