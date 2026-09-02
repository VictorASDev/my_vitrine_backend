package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.CreatorProfileRequest;
import com.myvitrine.api.dto.response.CreatorProfileResponse;
import com.myvitrine.api.dto.response.SocialNetworkResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.CreatorProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.SocialNetwork;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.model.enums.RegistrationStatus;
import com.myvitrine.api.repository.CreatorProfileRepository;
import com.myvitrine.api.repository.SocialNetworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CreatorProfileService {

    private final CreatorProfileRepository creatorProfileRepository;
    private final UserService userService;
    private final SocialNetworkRepository socialNetworkRepository;

    public CreatorProfileService(CreatorProfileRepository creatorProfileRepository, UserService userService,
                                 SocialNetworkRepository socialNetworkRepository) {
        this.creatorProfileRepository = creatorProfileRepository;
        this.userService = userService;
        this.socialNetworkRepository = socialNetworkRepository;
    }

    @Transactional
    public CreatorProfileResponse create(CreatorProfileRequest request) {
        if (request.userId() == null) {
            throw new BusinessRuleException("userId e obrigatorio para concluir o cadastro");
        }
        User user = userService.getUserOrThrow(request.userId());
        if (user.getRegistrationStatus() != RegistrationStatus.INCOMPLETE) {
            throw new BusinessRuleException("Cadastro do usuario " + user.getId() + " ja foi concluido");
        }
        if (user.getProfileType() != null) {
            throw new BusinessRuleException("Usuario " + user.getId() + " ja possui um profileType definido");
        }
        if (creatorProfileRepository.existsById(user.getId())) {
            throw new ResourceConflictException("Usuario " + user.getId() + " ja possui um perfil de criador");
        }
        CreatorProfile profile = new CreatorProfile(user, request.bio(), request.niche(), request.profilePhotoUrl());

        user.setProfileType(ProfileType.CREATOR);
        user.setRegistrationStatus(RegistrationStatus.COMPLETE);
        CreatorProfile saved = creatorProfileRepository.save(profile);
        replaceSocialNetworks(user, request.socialNetworks());
        return toResponse(saved);
    }

    public CreatorProfileResponse findById(UUID userId) {
        return toResponse(getCreatorProfileOrThrow(userId));
    }

    public List<CreatorProfileResponse> findAll() {
        return creatorProfileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public CreatorProfileResponse update(UUID userId, CreatorProfileRequest request) {
        CreatorProfile profile = getCreatorProfileOrThrow(userId);
        profile.setBio(request.bio());
        profile.setNiche(request.niche());
        profile.setProfilePhotoUrl(request.profilePhotoUrl());
        replaceSocialNetworks(profile.getUser(), request.socialNetworks());
        return toResponse(profile);
    }

    @Transactional
    public void delete(UUID userId) {
        CreatorProfile profile = getCreatorProfileOrThrow(userId);
        socialNetworkRepository.deleteAllByUser_Id(userId);
        creatorProfileRepository.delete(profile);
    }

    CreatorProfile getCreatorProfileOrThrow(UUID userId) {
        return creatorProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de criador nao encontrado: " + userId));
    }

    private CreatorProfileResponse toResponse(CreatorProfile profile) {
        List<SocialNetworkResponse> socialNetworks = socialNetworkRepository.findAllByUser_Id(profile.getUserId())
                .stream().map(SocialNetworkResponse::from).toList();
        return CreatorProfileResponse.from(profile, socialNetworks);
    }

    private void replaceSocialNetworks(User user, List<com.myvitrine.api.dto.request.SocialNetworkRequest> requests) {
        socialNetworkRepository.deleteAllByUser_Id(user.getId());
        if (requests != null) {
            socialNetworkRepository.saveAll(requests.stream()
                    .map(request -> new SocialNetwork(user, request.name(), request.url()))
                    .toList());
        }
    }
}
