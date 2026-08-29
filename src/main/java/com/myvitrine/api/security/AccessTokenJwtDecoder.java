package com.myvitrine.api.security;

import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * Decora um {@link JwtDecoder} generico exigindo que o token decodificado
 * tenha a claim {@code token_type=access}. Sem essa checagem, um refresh
 * token roubado (mesma assinatura, mesmo emissor) poderia ser enviado como
 * Bearer token e autenticar normalmente nos endpoints protegidos — este
 * decoder e o unico usado pelo resource server (SecurityConfig), enquanto
 * o decoder "cru" (sem essa restricao) e usado internamente por JwtService
 * para validar o refresh token no endpoint de refresh.
 */
public class AccessTokenJwtDecoder implements JwtDecoder {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";

    private final JwtDecoder delegate;

    public AccessTokenJwtDecoder(JwtDecoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        Jwt jwt = delegate.decode(token);
        if (!ACCESS_TOKEN_TYPE.equals(jwt.getClaimAsString(TOKEN_TYPE_CLAIM))) {
            throw new BadJwtException("O token informado nao e um access token valido");
        }
        return jwt;
    }
}
