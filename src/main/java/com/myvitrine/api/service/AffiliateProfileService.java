package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.AffiliateProfileRequest;
import com.myvitrine.api.dto.response.AffiliateProfileResponse;
import com.myvitrine.api.dto.response.SocialNetworkResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.AffiliateProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.SocialNetwork;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.model.enums.RegistrationStatus;
import com.myvitrine.api.repository.AffiliateProfileRepository;
import com.myvitrine.api.repository.SocialNetworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AffiliateProfileService {

    private final AffiliateProfileRepository affiliateProfileRepository;
    private final UserService userService;
    private final SocialNetworkRepository socialNetworkRepository;

    public AffiliateProfileService(AffiliateProfileRepository affiliateProfileRepository, UserService userService,
                                   SocialNetworkRepository socialNetworkRepository) {
        this.affiliateProfileRepository = affiliateProfileRepository;
        this.userService = userService;
        this.socialNetworkRepository = socialNetworkRepository;
    }

    @Transactional
    public AffiliateProfileResponse create(AffiliateProfileRequest request) {
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
        if (affiliateProfileRepository.existsById(user.getId())) {
            throw new ResourceConflictException("Usuario " + user.getId() + " ja possui um perfil de afiliado");
        }
        AffiliateProfile profile = new AffiliateProfile(user, request.bio(), request.niche(), request.profilePhotoUrl());
        user.setProfileType(ProfileType.AFFILIATE);
        user.setRegistrationStatus(RegistrationStatus.COMPLETE);
        AffiliateProfile saved = affiliateProfileRepository.save(profile);
        replaceSocialNetworks(user, request.socialNetworks());
        return toResponse(saved);
    }

    public AffiliateProfileResponse findById(UUID userId) {
        return toResponse(getAffiliateProfileOrThrow(userId));
    }

    public List<AffiliateProfileResponse> findAll() {
        return affiliateProfileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AffiliateProfileResponse update(UUID userId, AffiliateProfileRequest request) {
        AffiliateProfile profile = getAffiliateProfileOrThrow(userId);
        profile.setBio(request.bio());
        profile.setNiche(request.niche());
        profile.setProfilePhotoUrl(request.profilePhotoUrl());
        replaceSocialNetworks(profile.getUser(), request.socialNetworks());
        return toResponse(profile);
    }

    @Transactional
    public void delete(UUID userId) {
        AffiliateProfile profile = getAffiliateProfileOrThrow(userId);
        socialNetworkRepository.deleteAllByUser_Id(userId);
        affiliateProfileRepository.delete(profile);
    }

    AffiliateProfile getAffiliateProfileOrThrow(UUID userId) {
        return affiliateProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de afiliado nao encontrado: " + userId));
    }

    private AffiliateProfileResponse toResponse(AffiliateProfile profile) {
        List<SocialNetworkResponse> socialNetworks = socialNetworkRepository.findAllByUser_Id(profile.getUserId())
                .stream().map(SocialNetworkResponse::from).toList();
        return AffiliateProfileResponse.from(profile, socialNetworks);
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
