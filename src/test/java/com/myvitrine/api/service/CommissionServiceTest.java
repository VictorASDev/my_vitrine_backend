package com.myvitrine.api.service;

import com.myvitrine.api.dto.response.CommissionResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.AffiliateLink;
import com.myvitrine.api.model.AffiliateProfile;
import com.myvitrine.api.model.Commission;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.Sale;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.AffiliateLinkType;
import com.myvitrine.api.model.enums.PaymentStatus;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.CommissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommissionServiceTest {

    @Mock
    private CommissionRepository commissionRepository;

    @InjectMocks
    private CommissionService commissionService;

    private Sale saleWithAmountAndCommissionPercentage(BigDecimal saleAmount, BigDecimal commissionPercentage) {
        User storeUser = new User(UUID.randomUUID(), "Loja X", "loja@example.com", "hash", ProfileType.STORE, LocalDateTime.now());
        StoreProfile store = new StoreProfile(storeUser, "Loja X", null);
        Product product = new Product(UUID.randomUUID(), store, "Camiseta", new BigDecimal("100.00"),
                commissionPercentage, null, true, LocalDateTime.now());
        User affiliateUser = new User(UUID.randomUUID(), "Joao", "joao@example.com", "hash", ProfileType.AFFILIATE, LocalDateTime.now());
        AffiliateProfile affiliate = new AffiliateProfile(affiliateUser, null, null);
        AffiliateLink link = new AffiliateLink(UUID.randomUUID(), affiliate, product, "ABC12345",
                AffiliateLinkType.COUPON, LocalDateTime.now());
        return new Sale(UUID.randomUUID(), link, saleAmount, LocalDateTime.now());
    }

    @Test
    void shouldCalculateCommissionAndPlatformRetentionFromSaleAmount() {
        Sale sale = saleWithAmountAndCommissionPercentage(new BigDecimal("200.00"), new BigDecimal("10.00"));
        when(commissionRepository.save(any(Commission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommissionResponse response = commissionService.generateForSale(sale);

        // 10% de 200.00 = 20.00 (comissao); 20% de 20.00 = 4.00 (retencao da plataforma)
        assertThat(response.commissionAmount()).isEqualByComparingTo("20.00");
        assertThat(response.platformRetentionAmount()).isEqualByComparingTo("4.00");
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void shouldConfirmPendingCommission() {
        UUID id = UUID.randomUUID();
        Sale sale = saleWithAmountAndCommissionPercentage(new BigDecimal("100.00"), new BigDecimal("10.00"));
        Commission commission = new Commission(id, sale, new BigDecimal("10.00"), new BigDecimal("2.00"), PaymentStatus.PENDING);
        when(commissionRepository.findById(id)).thenReturn(Optional.of(commission));

        CommissionResponse response = commissionService.confirm(id);

        assertThat(response.status()).isEqualTo(PaymentStatus.CONFIRMED);
    }

    @Test
    void shouldThrowBusinessRuleWhenConfirmingAlreadyConfirmedCommission() {
        UUID id = UUID.randomUUID();
        Sale sale = saleWithAmountAndCommissionPercentage(new BigDecimal("100.00"), new BigDecimal("10.00"));
        Commission commission = new Commission(id, sale, new BigDecimal("10.00"), new BigDecimal("2.00"), PaymentStatus.CONFIRMED);
        when(commissionRepository.findById(id)).thenReturn(Optional.of(commission));

        assertThatThrownBy(() -> commissionService.confirm(id))
                .isInstanceOf(BusinessRuleException.class);
    }
}
