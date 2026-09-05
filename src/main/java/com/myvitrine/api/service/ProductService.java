package com.myvitrine.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myvitrine.api.dto.request.ProductRequest;
import com.myvitrine.api.dto.response.ProductResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.repository.ProductRepository;

@Service
public class ProductService {

    /** Percentual de comissao padrao aplicado quando o lojista nao informa um valor especifico. */
    private static final BigDecimal DEFAULT_COMMISSION_PERCENTAGE = new BigDecimal("10.00");

    private final ProductRepository productRepository;
    private final StoreProfileService storeProfileService;

    public ProductService(ProductRepository productRepository, StoreProfileService storeProfileService) {
        this.productRepository = productRepository;
        this.storeProfileService = storeProfileService;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        StoreProfile store = storeProfileService.getStoreProfileOrThrow(request.storeId());
        BigDecimal commissionPercentage = request.commissionPercentage() != null
                ? request.commissionPercentage()
                : DEFAULT_COMMISSION_PERCENTAGE;
        Product product = new Product(UUID.randomUUID(), store, request.name(), request.price(),
                commissionPercentage, request.imageUrl(), true, LocalDateTime.now());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse createForStore(ProductRequest request, UUID storeId) {
        if (!storeId.equals(request.storeId())) {
            throw new BusinessRuleException("O produto deve pertencer ao lojista autenticado");
        }
        return create(request);
    }

    public ProductResponse findById(UUID id) {
        return ProductResponse.from(getProductOrThrow(id));
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public List<ProductResponse> findByStore(UUID storeId) {
        return productRepository.findByStoreUserId(storeId).stream().map(ProductResponse::from).toList();
    }

    public Page<ProductResponse> findByStore(UUID storeId, Pageable pageable) {
        return productRepository.findByStoreUserId(storeId, pageable).map(ProductResponse::from);
    }

    public Page<ProductResponse> findOwned(UUID storeId, Pageable pageable) {
        return findByStore(storeId, pageable);
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getProductOrThrow(id);
        product.setName(request.name());
        product.setPrice(request.price());
        if (request.commissionPercentage() != null) {
            product.setCommissionPercentage(request.commissionPercentage());
        }
        product.setImageUrl(request.imageUrl());
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse updateOwned(UUID id, ProductRequest request, UUID storeId) {
        Product product = getOwnedProductOrThrow(id, storeId);
        if (!storeId.equals(request.storeId())) {
            throw new BusinessRuleException("O produto deve pertencer ao lojista autenticado");
        }
        product.setName(request.name());
        product.setPrice(request.price());
        if (request.commissionPercentage() != null) {
            product.setCommissionPercentage(request.commissionPercentage());
        }
        product.setImageUrl(request.imageUrl());
        return ProductResponse.from(product);
    }

    @Transactional
    public void deactivate(UUID id) {
        Product product = getProductOrThrow(id);
        product.setActive(false);
    }

    @Transactional
    public void deactivateOwned(UUID id, UUID storeId) {
        getOwnedProductOrThrow(id, storeId).setActive(false);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getProductOrThrow(id);
        productRepository.delete(product);
    }

    @Transactional
    public void deleteOwned(UUID id, UUID storeId) {
        productRepository.delete(getOwnedProductOrThrow(id, storeId));
    }

    private Product getOwnedProductOrThrow(UUID id, UUID storeId) {
        Product product = getProductOrThrow(id);
        if (!storeId.equals(product.getStore().getUserId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Voce so pode gerenciar seus proprios produtos");
        }
        return product;
    }

    Product getProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado: " + id));
    }
}
