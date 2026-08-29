package com.myvitrine.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.CreatorProfileRequest;
import com.myvitrine.api.dto.response.CreatorProfileResponse;
import com.myvitrine.api.service.CreatorProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreatorProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class CreatorProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreatorProfileService creatorProfileService;

    @Test
    void shouldReturnCreatedWhenCreatorProfileIsValid() throws Exception {
        UUID userId = UUID.randomUUID();
        CreatorProfileRequest request = new CreatorProfileRequest(userId, "bio", "http://portfolio.com");
        CreatorProfileResponse response = new CreatorProfileResponse(userId, "bio", "http://portfolio.com");

        when(creatorProfileService.create(any(CreatorProfileRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/creator-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.portfolioUrl").value("http://portfolio.com"));
    }
}
