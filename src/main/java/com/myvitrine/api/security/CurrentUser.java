package com.myvitrine.api.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

/**
 * Extrai o id do usuario autenticado a partir do subject do access token, e
 * garante posse de um recurso comparando esse id com o path variable
 * correspondente — usado pelos controllers que restringem update/delete ao
 * proprio dono do recurso (perfis e usuario).
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID id(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    public static void requireOwner(Jwt jwt, UUID resourceOwnerId) {
        if (!id(jwt).equals(resourceOwnerId)) {
            throw new AccessDeniedException("Voce so pode gerenciar os proprios dados");
        }
    }
}
