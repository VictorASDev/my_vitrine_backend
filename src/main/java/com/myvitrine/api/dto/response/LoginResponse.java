package com.myvitrine.api.dto.response;

/**
 * Resposta do login/refresh. O refresh token nunca aparece aqui — ele e
 * enviado exclusivamente como cookie httpOnly (ver RefreshTokenCookieFactory).
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserResponse user
) {
}
