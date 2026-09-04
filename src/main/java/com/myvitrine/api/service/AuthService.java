package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.LoginRequest;
import com.myvitrine.api.dto.response.LoginResponse;
import com.myvitrine.api.dto.response.UserResponse;
import com.myvitrine.api.exception.ConflictException;
import com.myvitrine.api.exception.InvalidTokenException;
import com.myvitrine.api.model.RefreshToken;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.RegistrationStatus;
import com.myvitrine.api.repository.RefreshTokenRepository;
import com.myvitrine.api.security.GeneratedRefreshToken;
import com.myvitrine.api.security.JwtProperties;
import com.myvitrine.api.security.JwtService;
import com.myvitrine.api.security.RefreshTokenCookieFactory;
import com.myvitrine.api.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Orquestra login, refresh (com rotacao) e logout. Os dois JWTs sao
 * emitidos por JwtService; este servico decide QUANDO emitir, persiste o
 * registro de controle do refresh token (RefreshTokenRepository) e escreve
 * o cookie httpOnly na resposta.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                        RefreshTokenRepository refreshTokenRepository,
                       JwtProperties jwtProperties, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
        this.userService = userService;
    }

    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletResponse response)
            throws ConflictException {

        User user = userService.findByUserByEmail(request.email());

        if (user.getRegistrationStatus() == RegistrationStatus.INCOMPLETE) {
            throw new ConflictException(
                    "Cadastro incompleto. Conclua seu cadastro antes de realizar o login."
            );
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        user = ((UserPrincipal) authentication.getPrincipal()).getUser();

        return issueTokens(user, response);
    }

    /**
     * Valida o refresh token do cookie, rotaciona (revoga o token usado e
     * emite um par novo) e retorna um novo access token. Se o token
     * apresentado ja tiver sido usado antes (revoked=true), trata como
     * possivel roubo/reuso e revoga TODOS os refresh tokens ativos do
     * usuario, forcando um novo login em todos os dispositivos.
     */
    @Transactional
    public LoginResponse refresh(String refreshTokenValue, HttpServletResponse response) {
        Jwt jwt = decodeRefreshTokenOrThrow(refreshTokenValue);
        UUID tokenId = UUID.fromString(jwt.getId());

        RefreshToken stored = refreshTokenRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new InvalidTokenException("Refresh token invalido"));

        if (stored.isRevoked()) {
            revokeAllActiveTokensForUser(stored.getUser().getId());
            throw new InvalidTokenException("Refresh token ja utilizado; todas as sessoes foram revogadas por seguranca");
        }
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expirado");
        }

        stored.setRevoked(true);

        return issueTokens(stored.getUser(), response);
    }

    @Transactional
    public void logout(String refreshTokenValue, HttpServletResponse response) {
        if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
            try {
                Jwt jwt = decodeRefreshTokenOrThrow(refreshTokenValue);
                UUID tokenId = UUID.fromString(jwt.getId());
                refreshTokenRepository.findByTokenId(tokenId).ifPresent(token -> token.setRevoked(true));
            } catch (InvalidTokenException ignored) {
                // logout deve ser idempotente mesmo com um cookie ja invalido/expirado/ausente
            }
        }
        response.addHeader(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.clear(jwtProperties.cookieSecure()).toString());
    }

    private LoginResponse issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(user);
        GeneratedRefreshToken refreshToken = jwtService.generateRefreshToken(user);

        RefreshToken entity = new RefreshToken(
                UUID.randomUUID(),
                user,
                refreshToken.tokenId(),
                LocalDateTime.ofInstant(refreshToken.expiresAt(), ZoneId.systemDefault()),
                false,
                LocalDateTime.now());
        refreshTokenRepository.save(entity);

        response.addHeader(HttpHeaders.SET_COOKIE,
                RefreshTokenCookieFactory.build(refreshToken.token(), jwtProperties.refreshTokenTtl(), jwtProperties.cookieSecure()).toString());

        return new LoginResponse(accessToken, "Bearer", jwtProperties.accessTokenTtl().toSeconds(), UserResponse.from(user));
    }

    private Jwt decodeRefreshTokenOrThrow(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new InvalidTokenException("Refresh token ausente");
        }
        try {
            Jwt jwt = jwtService.decodeAndValidate(refreshTokenValue);
            if (!jwtService.isRefreshToken(jwt)) {
                throw new InvalidTokenException("O token informado nao e um refresh token");
            }
            return jwt;
        } catch (JwtException e) {
            throw new InvalidTokenException("Refresh token invalido ou expirado");
        }
    }

    private void revokeAllActiveTokensForUser(UUID userId) {
        refreshTokenRepository.findByUserIdAndRevokedFalse(userId).forEach(token -> token.setRevoked(true));
    }
}
