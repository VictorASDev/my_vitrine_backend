package com.myvitrine.api.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.myvitrine.api.model.AffiliateLink;
import com.myvitrine.api.model.Commission;
import com.myvitrine.api.model.Hiring;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.Sale;
import com.myvitrine.api.model.enums.HiringStatus;
import com.myvitrine.api.model.enums.PaymentStatus;
import com.myvitrine.api.repository.AffiliateLinkRepository;
import com.myvitrine.api.repository.CommissionRepository;
import com.myvitrine.api.repository.HiringRepository;
import com.myvitrine.api.repository.ProductRepository;
import com.myvitrine.api.repository.SaleRepository;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private HiringRepository hiringRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private AffiliateLinkRepository affiliateLinkRepository;

    @Mock
    private CommissionRepository commissionRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void shouldAggregateStoreDashboardFromExistingRelations() {
        UUID storeId = UUID.randomUUID();
        Product active = org.mockito.Mockito.mock(Product.class);
        Product inactive = org.mockito.Mockito.mock(Product.class);
        Sale sale = org.mockito.Mockito.mock(Sale.class);
        Hiring requested = org.mockito.Mockito.mock(Hiring.class);
        Hiring approved = org.mockito.Mockito.mock(Hiring.class);

        when(active.isActive()).thenReturn(true);
        when(inactive.isActive()).thenReturn(false);
        when(sale.getAmount()).thenReturn(new BigDecimal("125.50"));
        when(requested.getStatus()).thenReturn(HiringStatus.REQUESTED);
        when(approved.getStatus()).thenReturn(HiringStatus.APPROVED);
        when(productRepository.findByStoreUserId(storeId)).thenReturn(List.of(active, inactive));
        when(hiringRepository.findByStoreUserId(storeId)).thenReturn(List.of(requested, approved));
        when(saleRepository.findByAffiliateLinkProductStoreUserId(storeId)).thenReturn(List.of(sale));

        var response = dashboardService.getStoreDashboard(storeId);

        assertThat(response.totalProducts()).isEqualTo(2);
        assertThat(response.activeProducts()).isEqualTo(1);
        assertThat(response.inactiveProducts()).isEqualTo(1);
        assertThat(response.totalSalesAmount()).isEqualByComparingTo("125.50");
        assertThat(response.totalHirings()).isEqualTo(2);
        assertThat(response.pendingHirings()).isEqualTo(1);
        assertThat(response.activeHirings()).isEqualTo(1);
        assertThat(response.completedHirings()).isEqualTo(1);
    }

    @Test
    void shouldAggregateAffiliateDashboardFromExistingRelations() {
        UUID affiliateId = UUID.randomUUID();
        AffiliateLink link = org.mockito.Mockito.mock(AffiliateLink.class);
        Sale sale = org.mockito.Mockito.mock(Sale.class);
        Commission pending = org.mockito.Mockito.mock(Commission.class);
        Commission confirmed = org.mockito.Mockito.mock(Commission.class);

        when(sale.getAmount()).thenReturn(new BigDecimal("200.00"));
        when(pending.getCommissionAmount()).thenReturn(new BigDecimal("20.00"));
        when(pending.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(confirmed.getCommissionAmount()).thenReturn(new BigDecimal("10.00"));
        when(confirmed.getStatus()).thenReturn(PaymentStatus.CONFIRMED);
        when(affiliateLinkRepository.findByAffiliateUserId(affiliateId)).thenReturn(List.of(link));
        when(saleRepository.findByAffiliateLinkAffiliateUserId(affiliateId)).thenReturn(List.of(sale));
        when(commissionRepository.findBySaleAffiliateLinkAffiliateUserId(affiliateId))
                .thenReturn(List.of(pending, confirmed));

        var response = dashboardService.getAffiliateDashboard(affiliateId);

        assertThat(response.totalLinks()).isEqualTo(1);
        assertThat(response.totalSales()).isEqualTo(1);
        assertThat(response.totalSalesAmount()).isEqualByComparingTo("200.00");
        assertThat(response.totalCommissions()).isEqualByComparingTo("30.00");
        assertThat(response.pendingCommissions()).isEqualByComparingTo("20.00");
        assertThat(response.confirmedCommissions()).isEqualByComparingTo("10.00");
    }
}
