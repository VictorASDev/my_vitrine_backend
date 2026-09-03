package com.myvitrine.api.controller;

import com.myvitrine.api.dto.response.CreatorFeeResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.enums.PaymentStatus;
import com.myvitrine.api.service.CreatorFeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreatorFeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class CreatorFeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreatorFeeService creatorFeeService;

    @Test
    void shouldConfirmFeeWhenHiringIsApproved() throws Exception {
        UUID id = UUID.randomUUID();
        CreatorFeeResponse response = new CreatorFeeResponse(id, UUID.randomUUID(), new BigDecimal("250.00"),
                new BigDecimal("50.00"), PaymentStatus.CONFIRMED);
        when(creatorFeeService.confirm(eq(id))).thenReturn(response);

        mockMvc.perform(patch("/api/creator-fees/{id}/confirm", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldReturnBadRequestWhenHiringIsNotYetApproved() throws Exception {
        UUID id = UUID.randomUUID();
        when(creatorFeeService.confirm(eq(id)))
                .thenThrow(new BusinessRuleException("Contratacao ainda nao aprovada"));

        mockMvc.perform(patch("/api/creator-fees/{id}/confirm", id))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnPaginatedFees() throws Exception {
        CreatorFeeResponse response = new CreatorFeeResponse(UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("250.00"), new BigDecimal("50.00"), PaymentStatus.PENDING);
        when(creatorFeeService.findAll(any())).thenReturn(new PageImpl<>(
                java.util.List.of(response), PageRequest.of(0, 1), 1));

        mockMvc.perform(get("/api/creator-fees").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(250.00))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
