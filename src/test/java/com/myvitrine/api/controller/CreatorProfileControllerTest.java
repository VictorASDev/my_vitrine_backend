package com.myvitrine.api.controller;

import tools.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.CreatorProfileRequest;
import com.myvitrine.api.dto.response.CreatorProfileResponse;
import com.myvitrine.api.service.CreatorProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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
        CreatorProfileRequest request = new CreatorProfileRequest(userId, "bio", "moda", null, "http://photo.com");
        CreatorProfileResponse response = new CreatorProfileResponse(userId, "bio", "moda", null, "http://photo.com");

        when(creatorProfileService.create(org.mockito.ArgumentMatchers.any(CreatorProfileRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/creator-profiles")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profilePhotoUrl").value("http://photo.com"));
    }
}
