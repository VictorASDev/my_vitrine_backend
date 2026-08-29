package com.myvitrine.api.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Beans de emissao (JwtEncoder) e validacao (JwtDecoder) de JWT, assinados
 * com o par RSA fornecido por {@link JwtKeyConfig}.
 *
 * Dois decoders sao expostos:
 * - {@code jwtDecoder}: valida apenas assinatura, expiracao e emissor —
 *   usado internamente por {@link JwtService} para decodificar refresh
 *   tokens (que carregam token_type=refresh, entao nao podem passar pelo
 *   decoder abaixo).
 * - {@code accessTokenJwtDecoder}: o mesmo, mas exigindo token_type=access
 *   — e o unico decoder registrado no resource server (SecurityConfig),
 *   protegendo os endpoints da API.
 */
@Configuration
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAPublicKey publicKey, JwtProperties jwtProperties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));
        return decoder;
    }

    @Bean
    public JwtDecoder accessTokenJwtDecoder(@Qualifier("jwtDecoder") JwtDecoder jwtDecoder) {
        return new AccessTokenJwtDecoder(jwtDecoder);
    }
}
