package com.myvitrine.api.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.myvitrine.api.dto.request.AffiliateLinkRequest;
import com.myvitrine.api.dto.response.AffiliateDashboardResponse;
import com.myvitrine.api.dto.response.AffiliateLinkResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.enums.AffiliateLinkType;
import com.myvitrine.api.service.AffiliateLinkService;
import com.myvitrine.api.service.DashboardService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AffiliateLinkController.class)
@AutoConfigureMockMvc(addFilters = false)
class AffiliateLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AffiliateLinkService affiliateLinkService;

        @MockitoBean
        private DashboardService dashboardService;

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

    @Test
    void shouldReturnAffiliateDashboardForAuthenticatedAffiliate() throws Exception {
        UUID affiliateId = UUID.randomUUID();
        when(dashboardService.getAffiliateDashboard(affiliateId)).thenReturn(
                new AffiliateDashboardResponse(2, 3, new BigDecimal("500.00"),
                        new BigDecimal("50.00"), new BigDecimal("30.00"), new BigDecimal("20.00")));

        mockMvc.perform(get("/api/affiliate-links/me/dashboard")
                        .with(jwt().jwt(builder -> builder.subject(affiliateId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLinks").value(2))
                .andExpect(jsonPath("$.confirmedCommissions").value(20.00));
    }
}
