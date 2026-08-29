package com.myvitrine.api.security;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * Monta o cookie httpOnly do refresh token. SameSite=Strict garante que o
 * navegador nunca envie esse cookie em requisicoes disparadas por outro
 * site (mitigacao de CSRF para /api/auth/refresh e /api/auth/logout, que
 * sao os unicos endpoints que o leem) — por isso a API pode manter CSRF
 * desabilitado globalmente (ver SecurityConfig).
 */
public final class RefreshTokenCookieFactory {

    private RefreshTokenCookieFactory() {
    }

    public static ResponseCookie build(String token, Duration maxAge, boolean secure) {
        return ResponseCookie.from(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(AuthConstants.REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }

    public static ResponseCookie clear(boolean secure) {
        return ResponseCookie.from(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path(AuthConstants.REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}
