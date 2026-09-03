package com.myvitrine.api.controller;

import tools.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.StoreProfileRequest;
import com.myvitrine.api.dto.response.StoreProfileResponse;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.service.StoreProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A camada de seguranca (SecurityFilterChain) esta desabilitada aqui
 * (addFilters=false); a autenticacao para os endpoints que dependem de
 * @AuthenticationPrincipal Jwt e simulada diretamente com o post-processor
 * jwt() do spring-security-test, que injeta a Authentication no contexto
 * independente do filtro real.
 */
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
        StoreProfileRequest request = new StoreProfileRequest(userId, "Loja X", "Descricao", "moda", "123", null);
        StoreProfileResponse response = new StoreProfileResponse(userId, "Loja X", "Descricao", "moda", "123", null);

        when(storeProfileService.create(any(StoreProfileRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/store-profiles")
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
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
        StoreProfileRequest request = new StoreProfileRequest(UUID.randomUUID(), "", null, null, null, null);

        mockMvc.perform(post("/api/store-profiles")
                        .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnForbiddenWhenUpdatingSomeoneElsesProfile() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID authenticatedId = UUID.randomUUID();
        StoreProfileRequest request = new StoreProfileRequest(ownerId, "Loja X", "Descricao", null, null, null);

        mockMvc.perform(put("/api/store-profiles/{userId}", ownerId)
                        .with(jwt().jwt(builder -> builder.subject(authenticatedId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowUpdatingOwnProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        StoreProfileRequest request = new StoreProfileRequest(userId, "Loja X", "Nova descricao", null, null, null);
        StoreProfileResponse response = new StoreProfileResponse(userId, "Loja X", "Nova descricao", null, null, null);

        when(storeProfileService.update(eq(userId), any(StoreProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/store-profiles/{userId}", userId)
                        .with(jwt().jwt(builder -> builder.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Nova descricao"));
    }
}
