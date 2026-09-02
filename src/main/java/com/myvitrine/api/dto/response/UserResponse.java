package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.model.enums.RegistrationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        ProfileType profileType,
        RegistrationStatus registrationStatus,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getProfileType(), user.getRegistrationStatus(), user.getCreatedAt());
    }
}
