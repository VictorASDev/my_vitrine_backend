package com.myvitrine.api.security;

import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtDecoder rawDecoder;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));
        JwtEncoder encoder = new NimbusJwtEncoder(jwkSource);

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("myvitrine-api-test"));
        this.rawDecoder = decoder;

        JwtProperties properties = new JwtProperties("myvitrine-api-test", Duration.ofMinutes(15), Duration.ofDays(7), null, null, false);
        this.jwtService = new JwtService(encoder, decoder, properties);
    }

    private User someUser() {
        return new User(UUID.randomUUID(), "Ana Lima", "ana@example.com", "hash", ProfileType.AFFILIATE, LocalDateTime.now());
    }

    @Test
    void accessTokenShouldCarryIdentityClaimsAndAccessType() {
        User user = someUser();

        String token = jwtService.generateAccessToken(user);
        Jwt decoded = rawDecoder.decode(token);

        assertThat(decoded.getSubject()).isEqualTo(user.getId().toString());
        assertThat(decoded.getClaimAsString("email")).isEqualTo("ana@example.com");
        assertThat(decoded.getClaimAsString("profileType")).isEqualTo("AFFILIATE");
        assertThat(decoded.getClaimAsString("token_type")).isEqualTo("access");
        assertThat(jwtService.isRefreshToken(decoded)).isFalse();
    }

    @Test
    void refreshTokenShouldCarryOnlySubjectJtiAndRefreshType() {
        User user = someUser();

        GeneratedRefreshToken generated = jwtService.generateRefreshToken(user);
        Jwt decoded = rawDecoder.decode(generated.token());

        assertThat(decoded.getSubject()).isEqualTo(user.getId().toString());
        assertThat(decoded.getId()).isEqualTo(generated.tokenId().toString());
        assertThat(jwtService.isRefreshToken(decoded)).isTrue();
        assertThat(decoded.getClaimAsString("email")).isNull();
    }

    @Test
    void accessTokenExpiryShouldMatchConfiguredTtl() {
        User user = someUser();

        String token = jwtService.generateAccessToken(user);
        Jwt decoded = rawDecoder.decode(token);

        Duration actualTtl = Duration.between(decoded.getIssuedAt(), decoded.getExpiresAt());
        assertThat(actualTtl).isCloseTo(Duration.ofMinutes(15), Duration.ofSeconds(2));
    }

    @Test
    void decodeAndValidateShouldRejectTamperedToken() {
        User user = someUser();
        String token = jwtService.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.decodeAndValidate(tampered))
                .isInstanceOf(org.springframework.security.oauth2.jwt.JwtException.class);
    }
}
