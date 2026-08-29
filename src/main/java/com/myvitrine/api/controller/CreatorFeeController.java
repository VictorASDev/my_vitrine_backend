package com.myvitrine.api.controller;

import com.myvitrine.api.dto.response.CreatorFeeResponse;
import com.myvitrine.api.service.CreatorFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/creator-fees")
@Tag(name = "Creator Fees", description = "Cache dos criadores de conteudo por contratacao")
public class CreatorFeeController {

    private final CreatorFeeService creatorFeeService;

    public CreatorFeeController(CreatorFeeService creatorFeeService) {
        this.creatorFeeService = creatorFeeService;
    }

    @GetMapping
    @Operation(summary = "Lista caches, opcionalmente filtrando por criador")
    public ResponseEntity<List<CreatorFeeResponse>> findAll(@RequestParam(required = false) UUID creatorId) {
        if (creatorId != null) {
            return ResponseEntity.ok(creatorFeeService.findByCreator(creatorId));
        }
        return ResponseEntity.ok(creatorFeeService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um cache pelo id")
    public ResponseEntity<CreatorFeeResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(creatorFeeService.findById(id));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirma o pagamento do cache (exige contratacao APPROVED)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cache ja confirmado ou contratacao ainda nao aprovada")
    public ResponseEntity<CreatorFeeResponse> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(creatorFeeService.confirm(id));
    }
}
