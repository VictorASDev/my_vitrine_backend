package com.myvitrine.api.controller;

import com.myvitrine.api.dto.request.LoginRequest;
import com.myvitrine.api.dto.response.LoginResponse;
import com.myvitrine.api.security.AuthConstants;
import com.myvitrine.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Login, renovacao e encerramento de sessao (JWT access + refresh)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica com e-mail/senha; retorna o access token no corpo e o refresh token em cookie httpOnly")
    @ApiResponse(responseCode = "401", description = "E-mail ou senha invalidos")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Emite um novo access token a partir do refresh token (cookie httpOnly); rotaciona o refresh token")
    @ApiResponse(responseCode = "401", description = "Refresh token ausente, invalido, expirado ou revogado")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = AuthConstants.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(refreshToken, response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoga o refresh token atual e limpa o cookie")
    public ResponseEntity<Void> logout(
            @CookieValue(name = AuthConstants.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken, response);
        return ResponseEntity.noContent().build();
    }
}
