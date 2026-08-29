package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.CreatorProfileRequest;
import com.myvitrine.api.dto.response.CreatorProfileResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.CreatorProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.CreatorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CreatorProfileService {

    private final CreatorProfileRepository creatorProfileRepository;
    private final UserService userService;

    public CreatorProfileService(CreatorProfileRepository creatorProfileRepository, UserService userService) {
        this.creatorProfileRepository = creatorProfileRepository;
        this.userService = userService;
    }

    @Transactional
    public CreatorProfileResponse create(CreatorProfileRequest request) {
        User user = userService.getUserOrThrow(request.userId());
        if (user.getProfileType() != ProfileType.CREATOR) {
            throw new BusinessRuleException("Usuario " + user.getId() + " nao possui profileType CREATOR");
        }
        if (creatorProfileRepository.existsById(user.getId())) {
            throw new ResourceConflictException("Usuario " + user.getId() + " ja possui um perfil de criador");
        }
        CreatorProfile profile = new CreatorProfile(user, request.bio(), request.portfolioUrl());
        return CreatorProfileResponse.from(creatorProfileRepository.save(profile));
    }

    public CreatorProfileResponse findById(UUID userId) {
        return CreatorProfileResponse.from(getCreatorProfileOrThrow(userId));
    }

    public List<CreatorProfileResponse> findAll() {
        return creatorProfileRepository.findAll().stream().map(CreatorProfileResponse::from).toList();
    }

    @Transactional
    public CreatorProfileResponse update(UUID userId, CreatorProfileRequest request) {
        CreatorProfile profile = getCreatorProfileOrThrow(userId);
        profile.setBio(request.bio());
        profile.setPortfolioUrl(request.portfolioUrl());
        return CreatorProfileResponse.from(profile);
    }

    @Transactional
    public void delete(UUID userId) {
        CreatorProfile profile = getCreatorProfileOrThrow(userId);
        creatorProfileRepository.delete(profile);
    }

    CreatorProfile getCreatorProfileOrThrow(UUID userId) {
        return creatorProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de criador nao encontrado: " + userId));
    }
}
