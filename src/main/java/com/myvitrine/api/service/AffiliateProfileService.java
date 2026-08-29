package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.AffiliateProfileRequest;
import com.myvitrine.api.dto.response.AffiliateProfileResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.AffiliateProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.AffiliateProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AffiliateProfileService {

    private final AffiliateProfileRepository affiliateProfileRepository;
    private final UserService userService;

    public AffiliateProfileService(AffiliateProfileRepository affiliateProfileRepository, UserService userService) {
        this.affiliateProfileRepository = affiliateProfileRepository;
        this.userService = userService;
    }

    /**
     * @param userId id do usuario autenticado (extraido do JWT pelo
     *               Controller) — o perfil criado e sempre o do proprio
     *               usuario, nunca de terceiros.
     */
    @Transactional
    public AffiliateProfileResponse create(UUID userId, AffiliateProfileRequest request) {
        User user = userService.getUserOrThrow(userId);
        if (user.getProfileType() != ProfileType.AFFILIATE) {
            throw new BusinessRuleException("Usuario " + user.getId() + " nao possui profileType AFFILIATE");
        }
        if (affiliateProfileRepository.existsById(user.getId())) {
            throw new ResourceConflictException("Usuario " + user.getId() + " ja possui um perfil de afiliado");
        }
        AffiliateProfile profile = new AffiliateProfile(user, request.bio(), request.niche());
        return AffiliateProfileResponse.from(affiliateProfileRepository.save(profile));
    }

    public AffiliateProfileResponse findById(UUID userId) {
        return AffiliateProfileResponse.from(getAffiliateProfileOrThrow(userId));
    }

    public List<AffiliateProfileResponse> findAll() {
        return affiliateProfileRepository.findAll().stream().map(AffiliateProfileResponse::from).toList();
    }

    @Transactional
    public AffiliateProfileResponse update(UUID userId, AffiliateProfileRequest request) {
        AffiliateProfile profile = getAffiliateProfileOrThrow(userId);
        profile.setBio(request.bio());
        profile.setNiche(request.niche());
        return AffiliateProfileResponse.from(profile);
    }

    @Transactional
    public void delete(UUID userId) {
        AffiliateProfile profile = getAffiliateProfileOrThrow(userId);
        affiliateProfileRepository.delete(profile);
    }

    AffiliateProfile getAffiliateProfileOrThrow(UUID userId) {
        return affiliateProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de afiliado nao encontrado: " + userId));
    }
}
