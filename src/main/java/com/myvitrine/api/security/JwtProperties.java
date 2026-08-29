package com.myvitrine.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuracao da camada de JWT (emissor, tempos de vida dos tokens, par de
 * chaves RSA usado para assinar/validar, e a flag "Secure" do cookie de
 * refresh token). Chaves RSA em branco fazem {@link JwtKeyConfig} gerar um
 * par efemero (apenas para desenvolvimento).
 */
@ConfigurationProperties(prefix = "myvitrine.security.jwt")
public record JwtProperties(
        @DefaultValue("myvitrine-api") String issuer,
        @DefaultValue("15m") Duration accessTokenTtl,
        @DefaultValue("7d") Duration refreshTokenTtl,
        String privateKey,
        String publicKey,
        @DefaultValue("true") boolean cookieSecure
) {
}
