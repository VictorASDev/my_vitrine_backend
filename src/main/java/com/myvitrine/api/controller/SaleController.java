package com.myvitrine.api.controller;

import com.myvitrine.api.dto.request.SaleRequest;
import com.myvitrine.api.dto.response.SaleResponse;
import com.myvitrine.api.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales", description = "Vendas rastreadas por links/cupons de afiliado")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @Operation(summary = "Registra uma venda a partir de um codigo de link/cupom e gera a comissao correspondente")
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saleService.create(request));
    }

    @GetMapping
    @Operation(summary = "Lista vendas paginadas, opcionalmente filtrando por link/cupom de afiliado")
    public ResponseEntity<Page<SaleResponse>> findAll(@RequestParam(required = false) UUID affiliateLinkId,
                                                       @PageableDefault(size = 20, sort = "saleDate") Pageable pageable) {
        if (affiliateLinkId != null) {
            return ResponseEntity.ok(saleService.findByAffiliateLink(affiliateLinkId, pageable));
        }
        return ResponseEntity.ok(saleService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma venda pelo id")
    public ResponseEntity<SaleResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(saleService.findById(id));
    }
}
