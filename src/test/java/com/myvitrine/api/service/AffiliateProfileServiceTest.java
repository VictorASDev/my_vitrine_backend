package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.AffiliateProfileRequest;
import com.myvitrine.api.dto.response.AffiliateProfileResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.AffiliateProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.AffiliateProfileRepository;
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
class AffiliateProfileServiceTest {

    @Mock
    private AffiliateProfileRepository affiliateProfileRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AffiliateProfileService affiliateProfileService;

    @Test
    void shouldCreateAffiliateProfileWhenUserIsAffiliateType() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Joao", "joao@example.com", "hash", ProfileType.AFFILIATE, LocalDateTime.now());
        AffiliateProfileRequest request = new AffiliateProfileRequest(userId, "bio", "moda");

        when(userService.getUserOrThrow(userId)).thenReturn(user);
        when(affiliateProfileRepository.existsById(userId)).thenReturn(false);
        when(affiliateProfileRepository.save(any(AffiliateProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AffiliateProfileResponse response = affiliateProfileService.create(request);

        assertThat(response.niche()).isEqualTo("moda");
    }

    @Test
    void shouldThrowBusinessRuleWhenUserIsNotAffiliateType() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Loja X", "loja@example.com", "hash", ProfileType.STORE, LocalDateTime.now());
        AffiliateProfileRequest request = new AffiliateProfileRequest(userId, "bio", "moda");

        when(userService.getUserOrThrow(userId)).thenReturn(user);

        assertThatThrownBy(() -> affiliateProfileService.create(request))
                .isInstanceOf(BusinessRuleException.class);
    }
}
