package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.AffiliateLinkRequest;
import com.myvitrine.api.dto.response.AffiliateLinkResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.AffiliateLink;
import com.myvitrine.api.model.AffiliateProfile;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.AffiliateLinkType;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.AffiliateLinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AffiliateLinkServiceTest {

    @Mock
    private AffiliateLinkRepository affiliateLinkRepository;

    @Mock
    private AffiliateProfileService affiliateProfileService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private AffiliateLinkService affiliateLinkService;

    private AffiliateProfile affiliateProfile() {
        User user = new User(UUID.randomUUID(), "Joao", "joao@example.com", "hash", ProfileType.AFFILIATE, LocalDateTime.now());
        return new AffiliateProfile(user, null, null);
    }

    private Product product(boolean active) {
        User storeUser = new User(UUID.randomUUID(), "Loja X", "loja@example.com", "hash", ProfileType.STORE, LocalDateTime.now());
        StoreProfile store = new StoreProfile(storeUser, "Loja X", null);
        return new Product(UUID.randomUUID(), store, "Camiseta", new BigDecimal("50.00"),
                new BigDecimal("10.00"), null, active, LocalDateTime.now());
    }

    @Test
    void shouldCreateAffiliateLinkWithUniqueGeneratedCode() {
        UUID affiliateId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        AffiliateLinkRequest request = new AffiliateLinkRequest(affiliateId, productId, AffiliateLinkType.LINK);

        when(affiliateProfileService.getAffiliateProfileOrThrow(affiliateId)).thenReturn(affiliateProfile());
        when(productService.getProductOrThrow(productId)).thenReturn(product(true));
        when(affiliateLinkRepository.existsByCode(any())).thenReturn(false);
        when(affiliateLinkRepository.save(any(AffiliateLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AffiliateLinkResponse response = affiliateLinkService.create(request);

        assertThat(response.code()).isNotBlank();
        assertThat(response.type()).isEqualTo(AffiliateLinkType.LINK);
    }

    @Test
    void shouldThrowBusinessRuleWhenProductIsInactive() {
        UUID affiliateId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        AffiliateLinkRequest request = new AffiliateLinkRequest(affiliateId, productId, AffiliateLinkType.COUPON);

        when(affiliateProfileService.getAffiliateProfileOrThrow(affiliateId)).thenReturn(affiliateProfile());
        when(productService.getProductOrThrow(productId)).thenReturn(product(false));

        assertThatThrownBy(() -> affiliateLinkService.create(request))
                .isInstanceOf(BusinessRuleException.class);
    }
}
