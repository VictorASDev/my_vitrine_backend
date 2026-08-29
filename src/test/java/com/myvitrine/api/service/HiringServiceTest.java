package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.HiringRequest;
import com.myvitrine.api.dto.response.CreatorFeeResponse;
import com.myvitrine.api.dto.response.HiringResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.CreatorProfile;
import com.myvitrine.api.model.Hiring;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.HiringStatus;
import com.myvitrine.api.model.enums.PaymentStatus;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.HiringRepository;
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
class HiringServiceTest {

    @Mock
    private HiringRepository hiringRepository;

    @Mock
    private StoreProfileService storeProfileService;

    @Mock
    private CreatorProfileService creatorProfileService;

    @Mock
    private ProductService productService;

    @Mock
    private CreatorFeeService creatorFeeService;

    @InjectMocks
    private HiringService hiringService;

    private StoreProfile storeProfile() {
        User user = new User(UUID.randomUUID(), "Loja X", "loja@example.com", "hash", ProfileType.STORE, LocalDateTime.now());
        return new StoreProfile(user, "Loja X", null);
    }

    private CreatorProfile creatorProfile() {
        User user = new User(UUID.randomUUID(), "Bia", "bia@example.com", "hash", ProfileType.CREATOR, LocalDateTime.now());
        return new CreatorProfile(user, null, null);
    }

    private Product product() {
        return new Product(UUID.randomUUID(), storeProfile(), "Camiseta", new BigDecimal("50.00"),
                new BigDecimal("10.00"), null, true, LocalDateTime.now());
    }

    @Test
    void shouldCreateHiringWithRequestedStatusAndGenerateDefaultCreatorFee() {
        UUID storeId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        HiringRequest request = new HiringRequest(storeId, creatorId, productId, null);

        when(storeProfileService.getStoreProfileOrThrow(storeId)).thenReturn(storeProfile());
        when(creatorProfileService.getCreatorProfileOrThrow(creatorId)).thenReturn(creatorProfile());
        when(productService.getProductOrThrow(productId)).thenReturn(product());
        when(hiringRepository.save(any(Hiring.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(creatorFeeService.generateForHiring(any(Hiring.class), any(BigDecimal.class))).thenReturn(
                new CreatorFeeResponse(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("250.00"),
                        new BigDecimal("50.00"), PaymentStatus.PENDING));

        HiringResponse response = hiringService.create(request);

        assertThat(response.status()).isEqualTo(HiringStatus.REQUESTED);

        ArgumentCaptor<BigDecimal> feeCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(creatorFeeService).generateForHiring(any(Hiring.class), feeCaptor.capture());
        assertThat(feeCaptor.getValue()).isEqualByComparingTo("250.00");
    }

    @Test
    void shouldAllowValidStatusTransition() {
        UUID id = UUID.randomUUID();
        Hiring hiring = new Hiring(id, storeProfile(), creatorProfile(), product(), HiringStatus.REQUESTED, LocalDateTime.now());
        when(hiringRepository.findById(id)).thenReturn(Optional.of(hiring));

        HiringResponse response = hiringService.updateStatus(id, HiringStatus.ACCEPTED);

        assertThat(response.status()).isEqualTo(HiringStatus.ACCEPTED);
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        UUID id = UUID.randomUUID();
        Hiring hiring = new Hiring(id, storeProfile(), creatorProfile(), product(), HiringStatus.REQUESTED, LocalDateTime.now());
        when(hiringRepository.findById(id)).thenReturn(Optional.of(hiring));

        assertThatThrownBy(() -> hiringService.updateStatus(id, HiringStatus.APPROVED))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldRejectTransitionFromTerminalApprovedStatus() {
        UUID id = UUID.randomUUID();
        Hiring hiring = new Hiring(id, storeProfile(), creatorProfile(), product(), HiringStatus.APPROVED, LocalDateTime.now());
        when(hiringRepository.findById(id)).thenReturn(Optional.of(hiring));

        assertThatThrownBy(() -> hiringService.updateStatus(id, HiringStatus.DELIVERED))
                .isInstanceOf(BusinessRuleException.class);
    }
}
