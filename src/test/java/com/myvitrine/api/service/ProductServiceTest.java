package com.myvitrine.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.myvitrine.api.dto.request.ProductRequest;
import com.myvitrine.api.dto.response.ProductResponse;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreProfileService storeProfileService;

    @InjectMocks
    private ProductService productService;

    private StoreProfile storeProfile() {
        User user = new User(UUID.randomUUID(), "Loja X", "loja@example.com", "hash", ProfileType.STORE, LocalDateTime.now());
        return new StoreProfile(user, "Loja X", null);
    }

    @Test
    void shouldApplyDefaultCommissionPercentageWhenNotInformed() {
        UUID storeId = UUID.randomUUID();
        StoreProfile store = storeProfile();
        ProductRequest request = new ProductRequest(storeId, "Camiseta", new BigDecimal("50.00"), null, null);

        when(storeProfileService.getStoreProfileOrThrow(storeId)).thenReturn(store);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(request);

        assertThat(response.commissionPercentage()).isEqualByComparingTo("10.00");
        assertThat(response.active()).isTrue();
    }

    @Test
    void shouldUseInformedCommissionPercentageWhenProvided() {
        UUID storeId = UUID.randomUUID();
        StoreProfile store = storeProfile();
        ProductRequest request = new ProductRequest(storeId, "Camiseta", new BigDecimal("50.00"), new BigDecimal("15.00"), null);

        when(storeProfileService.getStoreProfileOrThrow(storeId)).thenReturn(store);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.create(request);

        assertThat(response.commissionPercentage()).isEqualByComparingTo("15.00");
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnPaginatedProducts() {
        Pageable pageable = PageRequest.of(1, 1);
        Product product = new Product(UUID.randomUUID(), storeProfile(), "Camiseta", new BigDecimal("50.00"),
                new BigDecimal("10.00"), null, true, LocalDateTime.now());
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(java.util.List.of(product), pageable, 2));

        var page = productService.findAll(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).name()).isEqualTo("Camiseta");
        verify(productRepository).findAll(pageable);
    }

    @Test
    void shouldMarkProductAsInactiveWhenDeactivated() {
        UUID id = UUID.randomUUID();
        StoreProfile store = storeProfile();
        Product product = new Product(id, store, "Camiseta", new BigDecimal("50.00"),
                new BigDecimal("10.00"), null, true, LocalDateTime.now());
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        productService.deactivate(id);

        assertThat(product.isActive()).isFalse();
    }
}
