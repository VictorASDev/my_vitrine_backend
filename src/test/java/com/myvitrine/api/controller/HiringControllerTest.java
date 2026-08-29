package com.myvitrine.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.HiringRequest;
import com.myvitrine.api.dto.request.HiringStatusUpdateRequest;
import com.myvitrine.api.dto.response.HiringResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.enums.HiringStatus;
import com.myvitrine.api.service.HiringService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HiringController.class)
@AutoConfigureMockMvc(addFilters = false)
class HiringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HiringService hiringService;

    @Test
    void shouldReturnCreatedWhenHiringIsValid() throws Exception {
        UUID storeId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        HiringRequest request = new HiringRequest(storeId, creatorId, productId, null);
        HiringResponse response = new HiringResponse(UUID.randomUUID(), storeId, "Loja X", creatorId, productId,
                "Camiseta", HiringStatus.REQUESTED, LocalDateTime.now());

        when(hiringService.create(any(HiringRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/hirings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void shouldReturnBadRequestWhenStatusTransitionIsInvalid() throws Exception {
        UUID id = UUID.randomUUID();
        HiringStatusUpdateRequest request = new HiringStatusUpdateRequest(HiringStatus.APPROVED);
        when(hiringService.updateStatus(eq(id), eq(HiringStatus.APPROVED)))
                .thenThrow(new BusinessRuleException("Transicao de status invalida"));

        mockMvc.perform(patch("/api/hirings/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
