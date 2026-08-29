package com.myvitrine.api.controller;

import tools.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.request.LoginRequest;
import com.myvitrine.api.dto.response.LoginResponse;
import com.myvitrine.api.dto.response.UserResponse;
import com.myvitrine.api.exception.InvalidTokenException;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.security.AuthConstants;
import com.myvitrine.api.service.AuthService;
import jakarta.servlet.http.Cookie;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private UserResponse someUserResponse() {
        return new UserResponse(UUID.randomUUID(), "Ana Lima", "ana@example.com", ProfileType.STORE, LocalDateTime.now());
    }

    @Test
    void shouldReturnAccessTokenOnSuccessfulLogin() throws Exception {
        LoginRequest request = new LoginRequest("ana@example.com", "senha1234");
        LoginResponse response = new LoginResponse("access-token-value", "Bearer", 900L, someUserResponse());

        when(authService.login(any(LoginRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-value"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldReturnBadRequestWhenLoginPayloadIsInvalid() throws Exception {
        LoginRequest request = new LoginRequest("nao-e-email", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRefreshUsingCookie() throws Exception {
        LoginResponse response = new LoginResponse("new-access-token", "Bearer", 900L, someUserResponse());
        when(authService.refresh(eq("refresh-cookie-value"), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, "refresh-cookie-value")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshCookieIsInvalid() throws Exception {
        when(authService.refresh(eq("invalido"), any()))
                .thenThrow(new InvalidTokenException("Refresh token invalido"));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, "invalido")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshCookieIsMissing() throws Exception {
        when(authService.refresh(isNull(), any()))
                .thenThrow(new InvalidTokenException("Refresh token ausente"));

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNoContentOnLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, "refresh-cookie-value")))
                .andExpect(status().isNoContent());
    }
}
