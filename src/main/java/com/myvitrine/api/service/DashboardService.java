package com.myvitrine.api.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.myvitrine.api.dto.response.AffiliateDashboardResponse;
import com.myvitrine.api.dto.response.StoreDashboardResponse;
import com.myvitrine.api.model.enums.HiringStatus;
import com.myvitrine.api.model.enums.PaymentStatus;
import com.myvitrine.api.repository.AffiliateLinkRepository;
import com.myvitrine.api.repository.CommissionRepository;
import com.myvitrine.api.repository.HiringRepository;
import com.myvitrine.api.repository.ProductRepository;
import com.myvitrine.api.repository.SaleRepository;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final HiringRepository hiringRepository;
    private final SaleRepository saleRepository;
    private final AffiliateLinkRepository affiliateLinkRepository;
    private final CommissionRepository commissionRepository;

    public DashboardService(ProductRepository productRepository, HiringRepository hiringRepository,
                            SaleRepository saleRepository, AffiliateLinkRepository affiliateLinkRepository,
                            CommissionRepository commissionRepository) {
        this.productRepository = productRepository;
        this.hiringRepository = hiringRepository;
        this.saleRepository = saleRepository;
        this.affiliateLinkRepository = affiliateLinkRepository;
        this.commissionRepository = commissionRepository;
    }

    public StoreDashboardResponse getStoreDashboard(UUID storeId) {
        var products = productRepository.findByStoreUserId(storeId);
        var hirings = hiringRepository.findByStoreUserId(storeId);
        var sales = saleRepository.findByAffiliateLinkProductStoreUserId(storeId);

        return new StoreDashboardResponse(
                products.size(),
                products.stream().filter(product -> product.isActive()).count(),
                products.stream().filter(product -> !product.isActive()).count(),
                sales.size(),
                sales.stream().map(sale -> sale.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add),
                hirings.size(),
                hirings.stream().filter(hiring -> hiring.getStatus() == HiringStatus.REQUESTED).count(),
                hirings.stream().filter(hiring -> isActiveHiring(hiring.getStatus())).count(),
                hirings.stream().filter(hiring -> hiring.getStatus() == HiringStatus.APPROVED).count());
    }

    public AffiliateDashboardResponse getAffiliateDashboard(UUID affiliateId) {
        var sales = saleRepository.findByAffiliateLinkAffiliateUserId(affiliateId);
        var commissions = commissionRepository.findBySaleAffiliateLinkAffiliateUserId(affiliateId);

        return new AffiliateDashboardResponse(
                affiliateLinkRepository.findByAffiliateUserId(affiliateId).size(),
                sales.size(),
                sales.stream().map(sale -> sale.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add),
                commissions.stream().map(commission -> commission.getCommissionAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                commissions.stream().filter(commission -> commission.getStatus() == PaymentStatus.PENDING)
                        .map(commission -> commission.getCommissionAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                commissions.stream().filter(commission -> commission.getStatus() == PaymentStatus.CONFIRMED)
                        .map(commission -> commission.getCommissionAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private boolean isActiveHiring(HiringStatus status) {
        return status == HiringStatus.REQUESTED
                || status == HiringStatus.ACCEPTED
                || status == HiringStatus.IN_PRODUCTION
                || status == HiringStatus.DELIVERED;
    }
}