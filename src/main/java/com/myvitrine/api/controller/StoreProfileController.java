package com.myvitrine.api.controller;

import com.myvitrine.api.dto.request.StoreProfileRequest;
import com.myvitrine.api.dto.response.StoreProfileResponse;
import com.myvitrine.api.security.CurrentUser;
import com.myvitrine.api.service.StoreProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/store-profiles")
@Tag(name = "Store Profiles", description = "Perfis de lojista")
public class StoreProfileController {

    private final StoreProfileService storeProfileService;

    public StoreProfileController(StoreProfileService storeProfileService) {
        this.storeProfileService = storeProfileService;
    }
    @PostMapping
    @Operation(summary = "Cria o perfil de lojista e conclui o cadastro do usuario")
    public ResponseEntity<StoreProfileResponse> create(@Valid @RequestBody StoreProfileRequest request) {
        StoreProfileResponse response = storeProfileService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista todos os perfis de lojista")
    public ResponseEntity<List<StoreProfileResponse>> findAll() {
        return ResponseEntity.ok(storeProfileService.findAll());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Busca um perfil de lojista pelo id do usuario")
    public ResponseEntity<StoreProfileResponse> findById(@PathVariable UUID userId) {
        return ResponseEntity.ok(storeProfileService.findById(userId));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Atualiza um perfil de lojista")
    public ResponseEntity<StoreProfileResponse> update(@PathVariable UUID userId,
                                                         @Valid @RequestBody StoreProfileRequest request) {
        return ResponseEntity.ok(storeProfileService.update(userId, request));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove um perfil de lojista")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        storeProfileService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
