package com.myvitrine.api.controller;

import com.myvitrine.api.dto.request.CreatorProfileRequest;
import com.myvitrine.api.dto.response.CreatorProfileResponse;
import com.myvitrine.api.security.CurrentUser;
import com.myvitrine.api.service.CreatorProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/creator-profiles")
@Tag(name = "Creator Profiles", description = "Perfis de criador de conteudo")
public class CreatorProfileController {

    private final CreatorProfileService creatorProfileService;

    public CreatorProfileController(CreatorProfileService creatorProfileService) {
        this.creatorProfileService = creatorProfileService;
    }

    @PostMapping
    @Operation(summary = "Cria o perfil de criador e conclui o cadastro do usuario")
    public ResponseEntity<CreatorProfileResponse> create(@Valid @RequestBody CreatorProfileRequest request) {
        CreatorProfileResponse response = creatorProfileService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista todos os perfis de criador")
    public ResponseEntity<List<CreatorProfileResponse>> findAll() {
        return ResponseEntity.ok(creatorProfileService.findAll());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Busca um perfil de criador pelo id do usuario")
    public ResponseEntity<CreatorProfileResponse> findById(@PathVariable UUID userId) {
        return ResponseEntity.ok(creatorProfileService.findById(userId));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Atualiza um perfil de criador")
    public ResponseEntity<CreatorProfileResponse> update(@PathVariable UUID userId,
                                                           @Valid @RequestBody CreatorProfileRequest request,
                                                           HttpServletRequest httpRequest) {
        CurrentUser.requireOwner(httpRequest, userId);
        return ResponseEntity.ok(creatorProfileService.update(userId, request));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove um perfil de criador")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, HttpServletRequest httpRequest) {
        CurrentUser.requireOwner(httpRequest, userId);
        creatorProfileService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
