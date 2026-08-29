package com.myvitrine.api.controller;

import tools.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.AffiliateLinkRequest;
import com.myvitrine.api.dto.response.AffiliateLinkResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.enums.AffiliateLinkType;
import com.myvitrine.api.service.AffiliateLinkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AffiliateLinkController.class)
@AutoConfigureMockMvc(addFilters = false)
class AffiliateLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AffiliateLinkService affiliateLinkService;

    @Test
    void shouldReturnCreatedWhenAffiliateLinkIsValid() throws Exception {
        UUID affiliateId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        AffiliateLinkRequest request = new AffiliateLinkRequest(affiliateId, productId, AffiliateLinkType.LINK);
        AffiliateLinkResponse response = new AffiliateLinkResponse(UUID.randomUUID(), affiliateId, productId,
                "Camiseta", "ABC12345", AffiliateLinkType.LINK, LocalDateTime.now());

        when(affiliateLinkService.create(any(AffiliateLinkRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/affiliate-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ABC12345"));
    }

    @Test
    void shouldReturnBadRequestWhenProductIsInactive() throws Exception {
        AffiliateLinkRequest request = new AffiliateLinkRequest(UUID.randomUUID(), UUID.randomUUID(), AffiliateLinkType.COUPON);
        when(affiliateLinkService.create(any(AffiliateLinkRequest.class)))
                .thenThrow(new BusinessRuleException("Produto inativo"));

        mockMvc.perform(post("/api/affiliate-links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
