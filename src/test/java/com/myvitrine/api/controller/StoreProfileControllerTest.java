package com.myvitrine.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.StoreProfileRequest;
import com.myvitrine.api.dto.response.StoreProfileResponse;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.service.StoreProfileService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class StoreProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreProfileService storeProfileService;

    @Test
    void shouldReturnCreatedWhenStoreProfileIsValid() throws Exception {
        UUID userId = UUID.randomUUID();
        StoreProfileRequest request = new StoreProfileRequest(userId, "Loja X", "Descricao");
        StoreProfileResponse response = new StoreProfileResponse(userId, "Loja X", "Descricao");

        when(storeProfileService.create(any(StoreProfileRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/store-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storeName").value("Loja X"));
    }

    @Test
    void shouldReturnNotFoundWhenStoreProfileDoesNotExist() throws Exception {
        UUID userId = UUID.randomUUID();
        when(storeProfileService.findById(eq(userId)))
                .thenThrow(new ResourceNotFoundException("Perfil de lojista nao encontrado: " + userId));

        mockMvc.perform(get("/api/store-profiles/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenStoreNameIsBlank() throws Exception {
        StoreProfileRequest request = new StoreProfileRequest(UUID.randomUUID(), "", null);

        mockMvc.perform(post("/api/store-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
