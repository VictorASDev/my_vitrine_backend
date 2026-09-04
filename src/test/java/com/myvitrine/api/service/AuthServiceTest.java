package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.LoginRequest;
import com.myvitrine.api.dto.response.LoginResponse;
import com.myvitrine.api.exception.ConflictException;
import com.myvitrine.api.exception.InvalidTokenException;
import com.myvitrine.api.model.RefreshToken;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.model.enums.RegistrationStatus;
import com.myvitrine.api.repository.RefreshTokenRepository;
import com.myvitrine.api.security.GeneratedRefreshToken;
import com.myvitrine.api.security.JwtProperties;
import com.myvitrine.api.security.JwtService;
import com.myvitrine.api.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserService userService;

    private AuthService authService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties("myvitrine-api-test", Duration.ofMinutes(15), Duration.ofDays(7), null, null, false);
        authService = new AuthService(authenticationManager, jwtService, refreshTokenRepository, jwtProperties,
            userService);
    }

    private User someUser() {
        return new User(UUID.randomUUID(), "Ana Lima", "ana@example.com", "hash", ProfileType.STORE, LocalDateTime.now());
    }

    private Jwt refreshJwt(UUID tokenId) {
        return Jwt.withTokenValue("token-value")
                .header("alg", "RS256")
                .claim("jti", tokenId.toString())
                .claim("token_type", "refresh")
                .subject(UUID.randomUUID().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }

    @Test
    void shouldLoginAndSetRefreshCookieOnSuccess() {
        User user = someUser();
        UserPrincipal principal = new UserPrincipal(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(userService.findByUserByEmail("ana@example.com")).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token-value");
        when(jwtService.generateRefreshToken(user)).thenReturn(
                new GeneratedRefreshToken("refresh-token-value", UUID.randomUUID(), Instant.now().plusSeconds(3600)));

        LoginResponse loginResponse = authService.login(new LoginRequest("ana@example.com", "senha1234"), response);

        assertThat(loginResponse.accessToken()).isEqualTo("access-token-value");
        assertThat(loginResponse.tokenType()).isEqualTo("Bearer");
        assertThat(loginResponse.user().email()).isEqualTo("ana@example.com");

        verify(refreshTokenRepository).save(any(RefreshToken.class));
        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
        assertThat(cookieCaptor.getValue()).contains("refresh_token=refresh-token-value").contains("HttpOnly");
    }

    @Test
    void shouldPropagateAuthenticationExceptionOnBadCredentials() {
        User user = someUser();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(userService.findByUserByEmail("ana@example.com")).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Credenciais invalidas"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "errada"), response))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRejectLoginWhenRegistrationIsIncomplete() {
        User user = someUser();
        user.setRegistrationStatus(RegistrationStatus.INCOMPLETE);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(userService.findByUserByEmail("ana@example.com")).thenReturn(user);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@example.com", "senha1234"), response))
                .isInstanceOf(ConflictException.class);

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void shouldRotateRefreshTokenOnSuccessfulRefresh() {
        UUID tokenId = UUID.randomUUID();
        User user = someUser();
        RefreshToken stored = new RefreshToken(UUID.randomUUID(), user, tokenId,
                LocalDateTime.now().plusDays(1), false, LocalDateTime.now());
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtService.decodeAndValidate("refresh-cookie-value")).thenReturn(refreshJwt(tokenId));
        when(jwtService.isRefreshToken(any(Jwt.class))).thenReturn(true);
        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(stored));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn(
                new GeneratedRefreshToken("new-refresh-token", UUID.randomUUID(), Instant.now().plusSeconds(3600)));

        LoginResponse response1 = authService.refresh("refresh-cookie-value", response);

        assertThat(response1.accessToken()).isEqualTo("new-access-token");
        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldThrowInvalidTokenWhenRefreshCookieIsMissing() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThatThrownBy(() -> authService.refresh(null, response))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldThrowInvalidTokenWhenRefreshTokenSignatureIsInvalid() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(jwtService.decodeAndValidate("token-invalido")).thenThrow(new BadJwtException("assinatura invalida"));

        assertThatThrownBy(() -> authService.refresh("token-invalido", response))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldThrowInvalidTokenWhenRefreshTokenIsNotFoundInDatabase() {
        UUID tokenId = UUID.randomUUID();
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtService.decodeAndValidate("refresh-cookie-value")).thenReturn(refreshJwt(tokenId));
        when(jwtService.isRefreshToken(any(Jwt.class))).thenReturn(true);
        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("refresh-cookie-value", response))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldThrowInvalidTokenWhenRefreshTokenIsExpired() {
        UUID tokenId = UUID.randomUUID();
        User user = someUser();
        RefreshToken stored = new RefreshToken(UUID.randomUUID(), user, tokenId,
                LocalDateTime.now().minusDays(1), false, LocalDateTime.now());
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtService.decodeAndValidate("refresh-cookie-value")).thenReturn(refreshJwt(tokenId));
        when(jwtService.isRefreshToken(any(Jwt.class))).thenReturn(true);
        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh("refresh-cookie-value", response))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldRevokeAllActiveTokensWhenRefreshTokenReuseIsDetected() {
        UUID tokenId = UUID.randomUUID();
        User user = someUser();
        RefreshToken alreadyUsed = new RefreshToken(UUID.randomUUID(), user, tokenId,
                LocalDateTime.now().plusDays(1), true, LocalDateTime.now());
        RefreshToken otherActiveToken = new RefreshToken(UUID.randomUUID(), user, UUID.randomUUID(),
                LocalDateTime.now().plusDays(1), false, LocalDateTime.now());
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtService.decodeAndValidate("refresh-cookie-value")).thenReturn(refreshJwt(tokenId));
        when(jwtService.isRefreshToken(any(Jwt.class))).thenReturn(true);
        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(alreadyUsed));
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId())).thenReturn(List.of(otherActiveToken));

        assertThatThrownBy(() -> authService.refresh("refresh-cookie-value", response))
                .isInstanceOf(InvalidTokenException.class);

        assertThat(otherActiveToken.isRevoked()).isTrue();
        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void shouldRevokeStoredTokenAndClearCookieOnLogout() {
        UUID tokenId = UUID.randomUUID();
        User user = someUser();
        RefreshToken stored = new RefreshToken(UUID.randomUUID(), user, tokenId,
                LocalDateTime.now().plusDays(1), false, LocalDateTime.now());
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(jwtService.decodeAndValidate("refresh-cookie-value")).thenReturn(refreshJwt(tokenId));
        when(jwtService.isRefreshToken(any(Jwt.class))).thenReturn(true);
        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(stored));

        authService.logout("refresh-cookie-value", response);

        assertThat(stored.isRevoked()).isTrue();
        verify(response, times(1)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }

    @Test
    void shouldBeIdempotentWhenLoggingOutWithoutCookie() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        authService.logout(null, response);

        verify(refreshTokenRepository, never()).findByTokenId(any());
        verify(response, times(1)).addHeader(eq(HttpHeaders.SET_COOKIE), anyString());
    }
}
