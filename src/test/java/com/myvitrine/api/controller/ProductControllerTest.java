package com.myvitrine.api.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.myvitrine.api.dto.request.ProductRequest;
import com.myvitrine.api.dto.response.ProductResponse;
import com.myvitrine.api.dto.response.StoreDashboardResponse;
import com.myvitrine.api.service.DashboardService;
import com.myvitrine.api.service.ProductService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void shouldReturnCreatedWhenProductIsValid() throws Exception {
        UUID storeId = UUID.randomUUID();
        ProductRequest request = new ProductRequest(storeId, "Camiseta", new BigDecimal("50.00"), null, null);
        ProductResponse response = new ProductResponse(UUID.randomUUID(), storeId, "Loja X", "Camiseta",
                new BigDecimal("50.00"), new BigDecimal("10.00"), null, true, LocalDateTime.now());

        when(productService.createForStore(any(ProductRequest.class), org.mockito.ArgumentMatchers.eq(storeId)))
            .thenReturn(response);

        mockMvc.perform(post("/api/products")
                .with(authentication(new UsernamePasswordAuthenticationToken(storeId.toString(), null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Camiseta"));
    }

    @Test
    void shouldReturnBadRequestWhenPriceIsNotPositive() throws Exception {
        ProductRequest request = new ProductRequest(UUID.randomUUID(), "Camiseta", new BigDecimal("-1.00"), null, null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

            @Test
            void shouldReturnPaginatedProducts() throws Exception {
            UUID storeId = UUID.randomUUID();
            ProductResponse response = new ProductResponse(UUID.randomUUID(), storeId, "Loja X", "Camiseta",
                new BigDecimal("50.00"), new BigDecimal("10.00"), null, true, LocalDateTime.now());
            when(productService.findAll(any())).thenReturn(
                new PageImpl<>(List.of(response), PageRequest.of(1, 1), 2));

            mockMvc.perform(get("/api/products")
                    .param("page", "1")
                    .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Camiseta"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
            }

    @Test
    void shouldReturnNoContentWhenProductIsDeactivated() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/products/{id}/deactivate", id)
                .with(authentication(new UsernamePasswordAuthenticationToken(storeId.toString(), null))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnStoreDashboardForAuthenticatedStore() throws Exception {
        UUID storeId = UUID.randomUUID();
        when(dashboardService.getStoreDashboard(storeId)).thenReturn(
                new StoreDashboardResponse(2, 1, 1, 3, new BigDecimal("300.00"), 4, 1, 2, 1));

        mockMvc.perform(get("/api/products/me/dashboard")
                        .with(authentication(new UsernamePasswordAuthenticationToken(storeId.toString(), null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(2))
                .andExpect(jsonPath("$.totalSalesAmount").value(300.00));
    }
}
