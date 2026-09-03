package com.myvitrine.api.controller;

import tools.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.ProductRequest;
import com.myvitrine.api.dto.response.ProductResponse;
import com.myvitrine.api.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void shouldReturnCreatedWhenProductIsValid() throws Exception {
        UUID storeId = UUID.randomUUID();
        ProductRequest request = new ProductRequest(storeId, "Camiseta", new BigDecimal("50.00"), null, null);
        ProductResponse response = new ProductResponse(UUID.randomUUID(), storeId, "Loja X", "Camiseta",
                new BigDecimal("50.00"), new BigDecimal("10.00"), null, true, LocalDateTime.now());

        when(productService.create(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
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
    void shouldReturnNoContentWhenProductIsDeactivated() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/products/{id}/deactivate", id))
                .andExpect(status().isNoContent());
    }
}
