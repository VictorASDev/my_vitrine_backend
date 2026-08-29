package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.ProductRequest;
import com.myvitrine.api.dto.response.ProductResponse;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    public ProductResponse findById(UUID id) {
        return ProductResponse.from(getProductOrThrow(id));
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    public List<ProductResponse> findByStore(UUID storeId) {
        return productRepository.findByStoreUserId(storeId).stream().map(ProductResponse::from).toList();
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
    public void deactivate(UUID id) {
        Product product = getProductOrThrow(id);
        product.setActive(false);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getProductOrThrow(id);
        productRepository.delete(product);
    }

    Product getProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado: " + id));
    }
}
