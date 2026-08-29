package com.myvitrine.api.security;

import java.time.Instant;
import java.util.UUID;

/**
 * Resultado da geracao de um refresh token: o JWT propriamente dito (a ser
 * enviado como cookie httpOnly) e os dados necessarios para persistir o
 * registro de controle em RefreshToken (tokenId = claim "jti").
 */
public record GeneratedRefreshToken(String token, UUID tokenId, Instant expiresAt) {
}
