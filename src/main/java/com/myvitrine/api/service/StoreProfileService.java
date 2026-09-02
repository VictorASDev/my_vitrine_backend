package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.StoreProfileRequest;
import com.myvitrine.api.dto.response.StoreProfileResponse;
import com.myvitrine.api.dto.response.SocialNetworkResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.SocialNetwork;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.model.enums.RegistrationStatus;
import com.myvitrine.api.repository.StoreProfileRepository;
import com.myvitrine.api.repository.SocialNetworkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StoreProfileService {

    private final StoreProfileRepository storeProfileRepository;
    private final UserService userService;
    private final SocialNetworkRepository socialNetworkRepository;

    public StoreProfileService(StoreProfileRepository storeProfileRepository, UserService userService,
                               SocialNetworkRepository socialNetworkRepository) {
        this.storeProfileRepository = storeProfileRepository;
        this.userService = userService;
        this.socialNetworkRepository = socialNetworkRepository;
    }

    @Transactional
    public StoreProfileResponse create(StoreProfileRequest request) {
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
        if (storeProfileRepository.existsById(user.getId())) {
            throw new ResourceConflictException("Usuario " + user.getId() + " ja possui um perfil de lojista");
        }
        StoreProfile profile = new StoreProfile(user, request.storeName(), request.description(), request.niche(), request.cnpj());
        user.setProfileType(ProfileType.STORE);
        user.setRegistrationStatus(RegistrationStatus.COMPLETE);
        StoreProfile saved = storeProfileRepository.save(profile);
        replaceSocialNetworks(user, request.socialNetworks());
        return toResponse(saved);
    }

    public StoreProfileResponse findById(UUID userId) {
        return toResponse(getStoreProfileOrThrow(userId));
    }

    public List<StoreProfileResponse> findAll() {
        return storeProfileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public StoreProfileResponse update(UUID userId, StoreProfileRequest request) {
        StoreProfile profile = getStoreProfileOrThrow(userId);
        profile.setStoreName(request.storeName());
        profile.setDescription(request.description());
        profile.setNiche(request.niche());
        profile.setCnpj(request.cnpj());
        replaceSocialNetworks(profile.getUser(), request.socialNetworks());
        return toResponse(profile);
    }

    @Transactional
    public void delete(UUID userId) {
        StoreProfile profile = getStoreProfileOrThrow(userId);
        socialNetworkRepository.deleteAllByUser_Id(userId);
        storeProfileRepository.delete(profile);
    }

    StoreProfile getStoreProfileOrThrow(UUID userId) {
        return storeProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de lojista nao encontrado: " + userId));
    }

    private StoreProfileResponse toResponse(StoreProfile profile) {
        List<SocialNetworkResponse> socialNetworks = socialNetworkRepository.findAllByUser_Id(profile.getUserId())
                .stream().map(SocialNetworkResponse::from).toList();
        return StoreProfileResponse.from(profile, socialNetworks);
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
