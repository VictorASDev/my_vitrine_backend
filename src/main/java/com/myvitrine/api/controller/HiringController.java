package com.myvitrine.api.controller;

import com.myvitrine.api.dto.request.HiringRequest;
import com.myvitrine.api.dto.request.HiringStatusUpdateRequest;
import com.myvitrine.api.dto.response.HiringResponse;
import com.myvitrine.api.service.HiringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/hirings")
@Tag(name = "Hirings", description = "Contratacoes de criadores de conteudo por lojistas")
public class HiringController {

    private final HiringService hiringService;

    public HiringController(HiringService hiringService) {
        this.hiringService = hiringService;
    }

    @PostMapping
    @Operation(summary = "Cria uma contratacao (status inicial REQUESTED) e gera o cache do criador")
    public ResponseEntity<HiringResponse> create(@Valid @RequestBody HiringRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hiringService.create(request));
    }

    @GetMapping
    @Operation(summary = "Lista contratacoes paginadas, opcionalmente filtrando por lojista ou criador")
    public ResponseEntity<Page<HiringResponse>> findAll(@RequestParam(required = false) UUID storeId,
                                                        @RequestParam(required = false) UUID creatorId,
                                                        @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        if (storeId != null) {
            return ResponseEntity.ok(hiringService.findByStore(storeId, pageable));
        }
        if (creatorId != null) {
            return ResponseEntity.ok(hiringService.findByCreator(creatorId, pageable));
        }
        return ResponseEntity.ok(hiringService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma contratacao pelo id")
    public ResponseEntity<HiringResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(hiringService.findById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Avanca o status da contratacao (REQUESTED -> ACCEPTED -> IN_PRODUCTION -> DELIVERED -> APPROVED)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Transicao de status invalida")
    public ResponseEntity<HiringResponse> updateStatus(@PathVariable UUID id,
                                                         @Valid @RequestBody HiringStatusUpdateRequest request) {
        return ResponseEntity.ok(hiringService.updateStatus(id, request.status()));
    }
}
