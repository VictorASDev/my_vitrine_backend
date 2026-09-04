package com.myvitrine.api.service;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.myvitrine.api.dto.request.CreatorProfileRequest;
import com.myvitrine.api.dto.response.CreatorProfileResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.CreatorProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.model.enums.RegistrationStatus;
import com.myvitrine.api.repository.CreatorProfileRepository;
import com.myvitrine.api.repository.SocialNetworkRepository;

@ExtendWith(MockitoExtension.class)
class CreatorProfileServiceTest {

    @Mock
    private CreatorProfileRepository creatorProfileRepository;

    @Mock
    private UserService userService;

    @Mock
    private SocialNetworkRepository socialNetworkRepository;

    @InjectMocks
    private CreatorProfileService creatorProfileService;

    @Test
    void shouldCreateCreatorProfileForIncompleteUser() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Bia", "bia@example.com", "hash", null, LocalDateTime.now());
        CreatorProfileRequest request = new CreatorProfileRequest(userId, "bio", "moda", null, "http://photo.com");

        when(userService.getUserOrThrow(userId)).thenReturn(user);
        when(creatorProfileRepository.existsById(userId)).thenReturn(false);
        when(creatorProfileRepository.save(any(CreatorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreatorProfileResponse response = creatorProfileService.create(request);

        assertThat(response.profilePhotoUrl()).isEqualTo("http://photo.com");
        assertThat(user.getProfileType()).isEqualTo(ProfileType.CREATOR);
        assertThat(user.getRegistrationStatus()).isEqualTo(RegistrationStatus.COMPLETE);
    }

    @Test
    void shouldThrowBusinessRuleWhenUserRegistrationIsAlreadyComplete() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Loja X", "loja@example.com", "hash", ProfileType.STORE, LocalDateTime.now());
        CreatorProfileRequest request = new CreatorProfileRequest(userId, "bio", "moda", null, "http://photo.com");

        when(userService.getUserOrThrow(userId)).thenReturn(user);

        assertThatThrownBy(() -> creatorProfileService.create(request))
                .isInstanceOf(BusinessRuleException.class);
    }
}
