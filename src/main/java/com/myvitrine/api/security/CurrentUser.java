package com.myvitrine.api.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import jakarta.servlet.http.HttpServletRequest;
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

    public static UUID id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return id(authentication);
    }

    public static UUID id(HttpServletRequest request) {
        Authentication authentication = request.getUserPrincipal() instanceof Authentication principal
                ? principal : null;
        if (authentication == null) {
            var attributes = request.getAttributeNames();
            while (attributes.hasMoreElements()) {
                Object context = request.getAttribute(attributes.nextElement());
                if (context instanceof SecurityContext securityContext) {
                    authentication = securityContext.getAuthentication();
                    break;
                }
            }
        }
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        return id(authentication);
    }

    private static UUID id(Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("Usuario autenticado nao encontrado");
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return id(jwt);
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedException("Usuario autenticado nao encontrado");
        }
    }

    public static void requireOwner(Jwt jwt, UUID resourceOwnerId) {
        if (!id(jwt).equals(resourceOwnerId)) {
            throw new AccessDeniedException("Voce so pode gerenciar os proprios dados");
        }
    }

    public static void requireOwner(UUID resourceOwnerId) {
        if (!id().equals(resourceOwnerId)) {
            throw new AccessDeniedException("Voce so pode gerenciar os proprios dados");
        }
    }

    public static void requireOwner(HttpServletRequest request, UUID resourceOwnerId) {
        if (!id(request).equals(resourceOwnerId)) {
            throw new AccessDeniedException("Voce so pode gerenciar os proprios dados");
        }
    }
}
