package com.myvitrine.api.controller;

import tools.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.AffiliateProfileRequest;
import com.myvitrine.api.dto.response.AffiliateProfileResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.service.AffiliateProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AffiliateProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class AffiliateProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AffiliateProfileService affiliateProfileService;

    @Test
    void shouldReturnCreatedWhenAffiliateProfileIsValid() throws Exception {
        UUID userId = UUID.randomUUID();
        AffiliateProfileRequest request = new AffiliateProfileRequest("bio", "moda");
        AffiliateProfileResponse response = new AffiliateProfileResponse(userId, "bio", "moda");

        when(affiliateProfileService.create(eq(userId), any(AffiliateProfileRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/affiliate-profiles")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnBadRequestWhenUserProfileTypeIsNotAffiliate() throws Exception {
        UUID userId = UUID.randomUUID();
        AffiliateProfileRequest request = new AffiliateProfileRequest("bio", "moda");
        when(affiliateProfileService.create(eq(userId), any(AffiliateProfileRequest.class)))
                .thenThrow(new BusinessRuleException("Usuario nao possui profileType AFFILIATE"));

        mockMvc.perform(post("/api/affiliate-profiles")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
