package com.myvitrine.api.service;

import com.myvitrine.api.dto.response.CreatorFeeResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.CreatorFee;
import com.myvitrine.api.model.CreatorProfile;
import com.myvitrine.api.model.Hiring;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.HiringStatus;
import com.myvitrine.api.model.enums.PaymentStatus;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.CreatorFeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatorFeeServiceTest {

    @Mock
    private CreatorFeeRepository creatorFeeRepository;

    @InjectMocks
    private CreatorFeeService creatorFeeService;

    private Hiring hiringWithStatus(HiringStatus status) {
        User storeUser = new User(UUID.randomUUID(), "Loja X", "loja@example.com", "hash", ProfileType.STORE, LocalDateTime.now());
        StoreProfile store = new StoreProfile(storeUser, "Loja X", null);
        User creatorUser = new User(UUID.randomUUID(), "Bia", "bia@example.com", "hash", ProfileType.CREATOR, LocalDateTime.now());
        CreatorProfile creator = new CreatorProfile(creatorUser, null, null);
        Product product = new Product(UUID.randomUUID(), store, "Camiseta", new BigDecimal("50.00"),
                new BigDecimal("10.00"), null, true, LocalDateTime.now());
        return new Hiring(UUID.randomUUID(), store, creator, product, status, LocalDateTime.now());
    }

    @Test
    void shouldCalculatePlatformRetentionOverFeeAmount() {
        Hiring hiring = hiringWithStatus(HiringStatus.REQUESTED);
        when(creatorFeeRepository.save(any(CreatorFee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreatorFeeResponse response = creatorFeeService.generateForHiring(hiring, new BigDecimal("250.00"));

        // 20% de 250.00 = 50.00
        assertThat(response.platformRetentionAmount()).isEqualByComparingTo("50.00");
        assertThat(response.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void shouldConfirmFeeWhenHiringIsApproved() {
        UUID id = UUID.randomUUID();
        Hiring hiring = hiringWithStatus(HiringStatus.APPROVED);
        CreatorFee fee = new CreatorFee(id, hiring, new BigDecimal("250.00"), new BigDecimal("50.00"), PaymentStatus.PENDING);
        when(creatorFeeRepository.findById(id)).thenReturn(Optional.of(fee));

        CreatorFeeResponse response = creatorFeeService.confirm(id);

        assertThat(response.status()).isEqualTo(PaymentStatus.CONFIRMED);
    }

    @Test
    void shouldRejectConfirmationWhenHiringIsNotYetApproved() {
        UUID id = UUID.randomUUID();
        Hiring hiring = hiringWithStatus(HiringStatus.IN_PRODUCTION);
        CreatorFee fee = new CreatorFee(id, hiring, new BigDecimal("250.00"), new BigDecimal("50.00"), PaymentStatus.PENDING);
        when(creatorFeeRepository.findById(id)).thenReturn(Optional.of(fee));

        assertThatThrownBy(() -> creatorFeeService.confirm(id))
                .isInstanceOf(BusinessRuleException.class);
    }
}
