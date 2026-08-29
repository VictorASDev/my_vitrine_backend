package com.myvitrine.api.dto.response;

import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        ProfileType profileType,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getProfileType(), user.getCreatedAt());
    }
}
