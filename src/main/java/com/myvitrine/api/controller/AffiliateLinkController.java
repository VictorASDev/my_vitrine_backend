package com.myvitrine.api.controller;

import com.myvitrine.api.dto.request.AffiliateLinkRequest;
import com.myvitrine.api.dto.response.AffiliateLinkResponse;
import com.myvitrine.api.service.AffiliateLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/affiliate-links")
@Tag(name = "Affiliate Links", description = "Links e cupons gerados por afiliados")
public class AffiliateLinkController {

    private final AffiliateLinkService affiliateLinkService;

    public AffiliateLinkController(AffiliateLinkService affiliateLinkService) {
        this.affiliateLinkService = affiliateLinkService;
    }

    @PostMapping
    @Operation(summary = "Gera um novo link/cupom de afiliado para um produto")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Produto inativo")
    public ResponseEntity<AffiliateLinkResponse> create(@Valid @RequestBody AffiliateLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(affiliateLinkService.create(request));
    }

    @GetMapping
    @Operation(summary = "Lista links/cupons, opcionalmente filtrando por afiliado")
    public ResponseEntity<List<AffiliateLinkResponse>> findAll(@RequestParam(required = false) UUID affiliateId) {
        if (affiliateId != null) {
            return ResponseEntity.ok(affiliateLinkService.findByAffiliate(affiliateId));
        }
        return ResponseEntity.ok(affiliateLinkService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um link/cupom pelo id")
    public ResponseEntity<AffiliateLinkResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(affiliateLinkService.findById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um link/cupom")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        affiliateLinkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
