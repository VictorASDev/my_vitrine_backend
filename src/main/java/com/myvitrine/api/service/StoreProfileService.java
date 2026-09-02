package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.StoreProfileRequest;
import com.myvitrine.api.dto.response.StoreProfileResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.model.enums.RegistrationStatus;
import com.myvitrine.api.repository.StoreProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StoreProfileService {

    private final StoreProfileRepository storeProfileRepository;
    private final UserService userService;

    public StoreProfileService(StoreProfileRepository storeProfileRepository, UserService userService) {
        this.storeProfileRepository = storeProfileRepository;
        this.userService = userService;
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
        StoreProfile profile = new StoreProfile(user, request.storeName(), request.description());
        user.setProfileType(ProfileType.STORE);
        user.setRegistrationStatus(RegistrationStatus.COMPLETE);
        return StoreProfileResponse.from(storeProfileRepository.save(profile));
    }

    public StoreProfileResponse findById(UUID userId) {
        return StoreProfileResponse.from(getStoreProfileOrThrow(userId));
    }

    public List<StoreProfileResponse> findAll() {
        return storeProfileRepository.findAll().stream().map(StoreProfileResponse::from).toList();
    }

    @Transactional
    public StoreProfileResponse update(UUID userId, StoreProfileRequest request) {
        StoreProfile profile = getStoreProfileOrThrow(userId);
        profile.setStoreName(request.storeName());
        profile.setDescription(request.description());
        return StoreProfileResponse.from(profile);
    }

    @Transactional
    public void delete(UUID userId) {
        StoreProfile profile = getStoreProfileOrThrow(userId);
        storeProfileRepository.delete(profile);
    }

    StoreProfile getStoreProfileOrThrow(UUID userId) {
        return storeProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de lojista nao encontrado: " + userId));
    }
}
