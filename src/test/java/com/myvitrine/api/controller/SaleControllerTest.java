package com.myvitrine.api.controller;

import tools.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.SaleRequest;
import com.myvitrine.api.dto.response.SaleResponse;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.service.SaleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SaleController.class)
@AutoConfigureMockMvc(addFilters = false)
class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SaleService saleService;

    @Test
    void shouldReturnCreatedWhenSaleIsValid() throws Exception {
        SaleRequest request = new SaleRequest("ABC12345", new BigDecimal("200.00"));
        SaleResponse response = new SaleResponse(UUID.randomUUID(), UUID.randomUUID(), "ABC12345",
                new BigDecimal("200.00"), LocalDateTime.now());

        when(saleService.create(any(SaleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.affiliateLinkCode").value("ABC12345"));
    }

    @Test
    void shouldReturnNotFoundWhenAffiliateLinkCodeDoesNotExist() throws Exception {
        SaleRequest request = new SaleRequest("INEXISTENTE", new BigDecimal("200.00"));
        when(saleService.create(any(SaleRequest.class)))
                .thenThrow(new ResourceNotFoundException("Link/cupom nao encontrado"));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsNotPositive() throws Exception {
        SaleRequest request = new SaleRequest("ABC12345", new BigDecimal("0.00"));

        mockMvc.perform(post("/api/sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
