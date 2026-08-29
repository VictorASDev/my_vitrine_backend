package com.myvitrine.api.security;

import com.myvitrine.api.model.User;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Emite e decodifica os dois tipos de JWT da plataforma:
 * - access token: vida curta, claims de identidade (email/name/profileType),
 *   token_type=access, enviado ao cliente no corpo da resposta.
 * - refresh token: vida longa, apenas sub + jti, token_type=refresh, nunca
 *   exposto no corpo — vai sempre como cookie httpOnly (ver AuthService).
 */
@Service
public class JwtService {

    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_PROFILE_TYPE = "profileType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.accessTokenTtl()))
                .subject(user.getId().toString())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_NAME, user.getName())
                .claim(CLAIM_PROFILE_TYPE, user.getProfileType().name())
                .claim(CLAIM_TOKEN_TYPE, ACCESS_TOKEN_TYPE)
                .build();
        return encode(claims);
    }

    public GeneratedRefreshToken generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.refreshTokenTtl());
        UUID tokenId = UUID.randomUUID();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .id(tokenId.toString())
                .claim(CLAIM_TOKEN_TYPE, REFRESH_TOKEN_TYPE)
                .build();

        return new GeneratedRefreshToken(encode(claims), tokenId, expiresAt);
    }

    /**
     * Decodifica e valida assinatura/expiracao/emissor de qualquer token
     * emitido por este servico (access ou refresh) — quem chama e
     * responsavel por checar a claim token_type conforme o contexto de uso.
     * Lanca JwtException se o token for invalido/expirado.
     */
    public Jwt decodeAndValidate(String token) throws JwtException {
        return jwtDecoder.decode(token);
    }

    public boolean isRefreshToken(Jwt jwt) {
        return REFRESH_TOKEN_TYPE.equals(jwt.getClaimAsString(CLAIM_TOKEN_TYPE));
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
