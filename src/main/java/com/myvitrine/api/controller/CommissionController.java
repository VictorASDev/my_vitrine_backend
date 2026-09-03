package com.myvitrine.api.controller;

import com.myvitrine.api.dto.response.CommissionResponse;
import com.myvitrine.api.service.CommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/commissions")
@Tag(name = "Commissions", description = "Comissoes de afiliados geradas a partir de vendas")
public class CommissionController {

    private final CommissionService commissionService;

    public CommissionController(CommissionService commissionService) {
        this.commissionService = commissionService;
    }

    @GetMapping
    @Operation(summary = "Lista comissoes paginadas, opcionalmente filtrando por afiliado")
    public ResponseEntity<Page<CommissionResponse>> findAll(@RequestParam(required = false) UUID affiliateId,
                                                             @PageableDefault(size = 20) Pageable pageable) {
        if (affiliateId != null) {
            return ResponseEntity.ok(commissionService.findByAffiliate(affiliateId, pageable));
        }
        return ResponseEntity.ok(commissionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma comissao pelo id")
    public ResponseEntity<CommissionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(commissionService.findById(id));
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirma o pagamento de uma comissao")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Comissao ja confirmada")
    public ResponseEntity<CommissionResponse> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(commissionService.confirm(id));
    }
}
