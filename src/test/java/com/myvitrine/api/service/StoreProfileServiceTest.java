package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.StoreProfileRequest;
import com.myvitrine.api.dto.response.StoreProfileResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.StoreProfileRepository;
import com.myvitrine.api.repository.SocialNetworkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreProfileServiceTest {

    @Mock
    private StoreProfileRepository storeProfileRepository;

    @Mock
    private UserService userService;

    @Mock
    private SocialNetworkRepository socialNetworkRepository;

    @InjectMocks
    private StoreProfileService storeProfileService;

    @Test
    void shouldCreateStoreProfileWhenUserIsStoreType() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Loja X", "loja@example.com", "hash", null, LocalDateTime.now());
        StoreProfileRequest request = new StoreProfileRequest(userId, "Loja X", "Descricao", "moda", "123", null);

        when(userService.getUserOrThrow(userId)).thenReturn(user);
        when(storeProfileRepository.existsById(userId)).thenReturn(false);
        when(storeProfileRepository.save(any(StoreProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StoreProfileResponse response = storeProfileService.create(request);

        assertThat(response.storeName()).isEqualTo("Loja X");
        assertThat(response.userId()).isEqualTo(userId);
    }

    @Test
    void shouldThrowBusinessRuleWhenUserIsNotStoreType() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Afiliado X", "afiliado@example.com", "hash", ProfileType.AFFILIATE, LocalDateTime.now());
        StoreProfileRequest request = new StoreProfileRequest(userId, "Loja X", null, null, null, null);

        when(userService.getUserOrThrow(userId)).thenReturn(user);

        assertThatThrownBy(() -> storeProfileService.create(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void shouldThrowConflictWhenStoreProfileAlreadyExists() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Loja X", "loja@example.com", "hash", null, LocalDateTime.now());
        StoreProfileRequest request = new StoreProfileRequest(userId, "Loja X", null, null, null, null);

        when(userService.getUserOrThrow(userId)).thenReturn(user);
        when(storeProfileRepository.existsById(userId)).thenReturn(true);

        assertThatThrownBy(() -> storeProfileService.create(request))
                .isInstanceOf(ResourceConflictException.class);
    }
}
