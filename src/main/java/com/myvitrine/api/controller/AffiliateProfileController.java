package com.myvitrine.api.controller;

import com.myvitrine.api.dto.request.AffiliateProfileRequest;
import com.myvitrine.api.dto.response.AffiliateProfileResponse;
import com.myvitrine.api.security.CurrentUser;
import com.myvitrine.api.service.AffiliateProfileService;
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
@RequestMapping("/api/affiliate-profiles")
@Tag(name = "Affiliate Profiles", description = "Perfis de afiliado")
public class AffiliateProfileController {

    private final AffiliateProfileService affiliateProfileService;

    public AffiliateProfileController(AffiliateProfileService affiliateProfileService) {
        this.affiliateProfileService = affiliateProfileService;
    }

    @PostMapping
    @Operation(summary = "Cria o perfil de afiliado do usuario autenticado (requer ROLE_AFFILIATE)")
    public ResponseEntity<AffiliateProfileResponse> create(@Valid @RequestBody AffiliateProfileRequest request,
                                                           @AuthenticationPrincipal Jwt jwt) {
        AffiliateProfileResponse response = affiliateProfileService.create(CurrentUser.id(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista todos os perfis de afiliado")
    public ResponseEntity<List<AffiliateProfileResponse>> findAll() {
        return ResponseEntity.ok(affiliateProfileService.findAll());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Busca um perfil de afiliado pelo id do usuario")
    public ResponseEntity<AffiliateProfileResponse> findById(@PathVariable UUID userId) {
        return ResponseEntity.ok(affiliateProfileService.findById(userId));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Atualiza um perfil de afiliado")
    public ResponseEntity<AffiliateProfileResponse> update(@PathVariable UUID userId,
                                                             @Valid @RequestBody AffiliateProfileRequest request) {
        return ResponseEntity.ok(affiliateProfileService.update(userId, request));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove um perfil de afiliado")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        affiliateProfileService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
