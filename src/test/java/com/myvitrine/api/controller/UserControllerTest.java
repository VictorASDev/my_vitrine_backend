package com.myvitrine.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.UserRequest;
import com.myvitrine.api.dto.response.UserResponse;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A camada de seguranca (Spring Security/OAuth2) esta fora do escopo desta
 * entrega, entao os filtros de seguranca sao desabilitados nestes testes de
 * controller para nao colidir com uma futura configuracao de autenticacao.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturnCreatedWhenUserIsValid() throws Exception {
        UserRequest request = new UserRequest("Ana Lima", "ana@example.com", "senha1234", ProfileType.STORE);
        UUID id = UUID.randomUUID();
        UserResponse response = new UserResponse(id, "Ana Lima", "ana@example.com", ProfileType.STORE, LocalDateTime.now());

        when(userService.create(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        UserRequest request = new UserRequest("Ana Lima", "email-invalido", "senha1234", ProfileType.STORE);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        UserRequest request = new UserRequest("Ana Lima", "ana@example.com", "senha1234", ProfileType.STORE);
        when(userService.create(any(UserRequest.class)))
                .thenThrow(new ResourceConflictException("E-mail ja cadastrado"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.findById(eq(id))).thenThrow(new ResourceNotFoundException("Usuario nao encontrado: " + id));

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}
