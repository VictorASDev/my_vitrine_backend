package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.SaleRequest;
import com.myvitrine.api.dto.response.CommissionResponse;
import com.myvitrine.api.dto.response.SaleResponse;
import com.myvitrine.api.model.AffiliateLink;
import com.myvitrine.api.model.AffiliateProfile;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.Sale;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.AffiliateLinkType;
import com.myvitrine.api.model.enums.PaymentStatus;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.SaleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private AffiliateLinkService affiliateLinkService;

    @Mock
    private CommissionService commissionService;

    @InjectMocks
    private SaleService saleService;

    @Test
    void shouldRegisterSaleAndTriggerCommissionGeneration() {
        User storeUser = new User(UUID.randomUUID(), "Loja X", "loja@example.com", "hash", ProfileType.STORE, LocalDateTime.now());
        StoreProfile store = new StoreProfile(storeUser, "Loja X", null);
        Product product = new Product(UUID.randomUUID(), store, "Camiseta", new BigDecimal("100.00"),
                new BigDecimal("10.00"), null, true, LocalDateTime.now());
        User affiliateUser = new User(UUID.randomUUID(), "Joao", "joao@example.com", "hash", ProfileType.AFFILIATE, LocalDateTime.now());
        AffiliateProfile affiliate = new AffiliateProfile(affiliateUser, null, null);
        AffiliateLink link = new AffiliateLink(UUID.randomUUID(), affiliate, product, "ABC12345",
                AffiliateLinkType.COUPON, LocalDateTime.now());

        SaleRequest request = new SaleRequest("ABC12345", new BigDecimal("200.00"));

        when(affiliateLinkService.getAffiliateLinkByCodeOrThrow("ABC12345")).thenReturn(link);
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commissionService.generateForSale(any(Sale.class))).thenReturn(
                new CommissionResponse(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("20.00"),
                        new BigDecimal("4.00"), PaymentStatus.PENDING));

        SaleResponse response = saleService.create(request);

        assertThat(response.amount()).isEqualByComparingTo("200.00");
        assertThat(response.affiliateLinkCode()).isEqualTo("ABC12345");

        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(commissionService).generateForSale(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("200.00");
    }
}
