package com.myvitrine.api.security;

public final class AuthConstants {

    /** Nome do cookie httpOnly que carrega o refresh token. */
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";

    /** Escopo do cookie: so e enviado pelo navegador para /api/auth/**, reduzindo a superficie de exposicao. */
    public static final String REFRESH_TOKEN_COOKIE_PATH = "/api/auth";

    private AuthConstants() {
    }
}
