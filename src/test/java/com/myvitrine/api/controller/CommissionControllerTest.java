package com.myvitrine.api.controller;

import com.myvitrine.api.dto.response.CommissionResponse;
import com.myvitrine.api.exception.BusinessRuleException;
import com.myvitrine.api.model.enums.PaymentStatus;
import com.myvitrine.api.service.CommissionService;
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

@WebMvcTest(CommissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommissionService commissionService;

    @Test
    void shouldConfirmPendingCommission() throws Exception {
        UUID id = UUID.randomUUID();
        CommissionResponse response = new CommissionResponse(id, UUID.randomUUID(), new BigDecimal("20.00"),
                new BigDecimal("4.00"), PaymentStatus.CONFIRMED);
        when(commissionService.confirm(eq(id))).thenReturn(response);

        mockMvc.perform(patch("/api/commissions/{id}/confirm", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldReturnBadRequestWhenCommissionAlreadyConfirmed() throws Exception {
        UUID id = UUID.randomUUID();
        when(commissionService.confirm(eq(id))).thenThrow(new BusinessRuleException("Comissao ja confirmada"));

        mockMvc.perform(patch("/api/commissions/{id}/confirm", id))
                .andExpect(status().isBadRequest());
    }

            @Test
            void shouldReturnPaginatedCommissions() throws Exception {
            CommissionResponse response = new CommissionResponse(UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("20.00"), new BigDecimal("4.00"), PaymentStatus.PENDING);
            when(commissionService.findAll(any())).thenReturn(new PageImpl<>(
                java.util.List.of(response), PageRequest.of(0, 1), 1));

            mockMvc.perform(get("/api/commissions").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"))
                .andExpect(jsonPath("$.totalElements").value(1));
            }
}
