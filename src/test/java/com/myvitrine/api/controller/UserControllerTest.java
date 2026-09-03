package com.myvitrine.api.controller;

import com.myvitrine.api.model.enums.RegistrationStatus;
import tools.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.UserRequest;
import com.myvitrine.api.dto.response.UserResponse;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A camada de seguranca (SecurityFilterChain) esta desabilitada aqui
 * (addFilters=false); a autenticacao para os endpoints que dependem de
 * @AuthenticationPrincipal Jwt (update/delete) e simulada com o
 * post-processor jwt() do spring-security-test.
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
        UserRequest request = new UserRequest("Ana Lima", "ana@example.com", "senha1234");
        UUID id = UUID.randomUUID();
        UserResponse response = new UserResponse(id, "Ana Lima", "ana@example.com", ProfileType.STORE, RegistrationStatus.INCOMPLETE, LocalDateTime.now());

        when(userService.create(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        UserRequest request = new UserRequest("Ana Lima", "email-invalido", "senha1234");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        UserRequest request = new UserRequest("Ana Lima", "ana@example.com", "senha1234");
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

    @Test
    void shouldAllowUserToUpdateOwnAccount() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequest request = new UserRequest("Ana Lima", "ana@example.com", "novaSenha123");
        UserResponse response = new UserResponse(id, "Ana Lima", "ana@example.com", ProfileType.STORE, RegistrationStatus.INCOMPLETE, LocalDateTime.now());

        when(userService.update(eq(id), any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/{id}", id)
                        .with(jwt().jwt(builder -> builder.subject(id.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnForbiddenWhenUpdatingAnotherUsersAccount() throws Exception {
        UUID targetId = UUID.randomUUID();
        UUID authenticatedId = UUID.randomUUID();
        UserRequest request = new UserRequest("Ana Lima", "ana@example.com", "novaSenha123");

        mockMvc.perform(put("/api/users/{id}", targetId)
                        .with(jwt().jwt(builder -> builder.subject(authenticatedId.toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnForbiddenWhenDeletingAnotherUsersAccount() throws Exception {
        UUID targetId = UUID.randomUUID();
        UUID authenticatedId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/{id}", targetId)
                        .with(jwt().jwt(builder -> builder.subject(authenticatedId.toString()))))
                .andExpect(status().isForbidden());
    }
}
