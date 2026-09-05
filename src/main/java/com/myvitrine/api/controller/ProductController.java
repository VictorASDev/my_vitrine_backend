package com.myvitrine.api.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myvitrine.api.dto.request.ProductRequest;
import com.myvitrine.api.dto.response.ProductResponse;
import com.myvitrine.api.dto.response.StoreDashboardResponse;
import com.myvitrine.api.security.CurrentUser;
import com.myvitrine.api.service.DashboardService;
import com.myvitrine.api.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Catalogo de produtos dos lojistas")
public class ProductController {

    private final ProductService productService;
    private final DashboardService dashboardService;

    public ProductController(ProductService productService, DashboardService dashboardService) {
        this.productService = productService;
        this.dashboardService = dashboardService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo produto")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request,
                                                   HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                productService.createForStore(request, CurrentUser.id(httpRequest)));
    }

    @GetMapping("/me")
    @Operation(summary = "Lista os produtos do lojista autenticado")
    public ResponseEntity<Page<ProductResponse>> findMyProducts(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
                                                                 HttpServletRequest httpRequest) {
        return ResponseEntity.ok(productService.findOwned(CurrentUser.id(httpRequest), pageable));
    }

    @GetMapping("/me/dashboard")
    @Operation(summary = "Exibe o resumo do dashboard do lojista autenticado")
    public ResponseEntity<StoreDashboardResponse> findMyDashboard(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(dashboardService.getStoreDashboard(CurrentUser.id(httpRequest)));
    }

    @GetMapping
    @Operation(summary = "Lista produtos paginados, opcionalmente filtrando por lojista")
    public ResponseEntity<Page<ProductResponse>> findAll(@RequestParam(required = false) UUID storeId,
                                                         @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        if (storeId != null) {
            return ResponseEntity.ok(productService.findByStore(storeId, pageable));
        }
        return ResponseEntity.ok(productService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produto pelo id")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um produto")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request,
                                                   HttpServletRequest httpRequest) {
        return ResponseEntity.ok(productService.updateOwned(id, request, CurrentUser.id(httpRequest)));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativa um produto (deixa de aceitar novos links/cupons)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id, HttpServletRequest httpRequest) {
        productService.deactivateOwned(id, CurrentUser.id(httpRequest));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um produto")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest httpRequest) {
        productService.deleteOwned(id, CurrentUser.id(httpRequest));
        return ResponseEntity.noContent().build();
    }
}
