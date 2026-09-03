package com.myvitrine.api.controller;

import tools.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.HiringRequest;
import com.myvitrine.api.dto.request.HiringStatusUpdateRequest;
import com.myvitrine.api.dto.response.HiringResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.enums.HiringStatus;
import com.myvitrine.api.service.HiringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void shouldReturnPaginatedHirings() throws Exception {
        HiringResponse response = new HiringResponse(UUID.randomUUID(), UUID.randomUUID(), "Loja X",
                UUID.randomUUID(), UUID.randomUUID(), "Camiseta", HiringStatus.REQUESTED, LocalDateTime.now());
        when(hiringService.findAll(any())).thenReturn(new PageImpl<>(
                java.util.List.of(response), PageRequest.of(0, 1), 1));

        mockMvc.perform(get("/api/hirings").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("REQUESTED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
