package com.myvitrine.api.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Fio condutor da camada de autorizacao: API stateless autenticada por
 * Bearer JWT (access token). O refresh token nunca passa por aqui — ele
 * so e lido manualmente pelo AuthController/AuthService a partir do
 * cookie httpOnly, nos endpoints de /api/auth/**.
 *
 * Regras de acesso (authorizeHttpRequests e avaliado na ordem declarada,
 * a primeira regra que casar decide):
 * - /api/auth/**, POST /api/users (auto-cadastro) e docs: publicos.
 * - Endpoints de criacao especificos de um profileType exigem o ROLE
 *   correspondente (mapeado a partir da claim "profileType" do JWT).
 * - Todo o restante de /api/** exige apenas estar autenticado; checagens
 *   de posse do proprio recurso (ex.: so o dono pode editar seu perfil)
 *   ficam no Controller, por precisarem comparar o subject do JWT com um
 *   path variable — ver StoreProfileController/AffiliateProfileController/
 *   CreatorProfileController/UserController.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /**
     * Mapeia a claim customizada "profileType" (STORE/AFFILIATE/CREATOR)
     * para uma GrantedAuthority "ROLE_<profileType>", permitindo usar
     * hasRole(...) tanto em authorizeHttpRequests quanto em @PreAuthorize.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String profileType = jwt.getClaimAsString("profileType");
            if (profileType == null || profileType.isBlank()) {
                return List.of();
            }
            return List.of(new SimpleGrantedAuthority("ROLE_" + profileType));
        });
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                     @Qualifier("accessTokenJwtDecoder") JwtDecoder accessTokenJwtDecoder,
                                                     JwtAuthenticationConverter jwtAuthenticationConverter,
                                                     RestAuthenticationEntryPoint authenticationEntryPoint,
                                                     RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                // API stateless com JWT em Authorization: Bearer — sem sessao, sem CSRF token classico.
                // O unico endpoint autenticado por cookie (refresh/logout) usa SameSite=Strict, que ja
                // impede o navegador de enviar o cookie em requisicoes cross-site (mitigacao de CSRF).
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/store-profiles").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/affiliate-profiles").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/creator-profiles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/email/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("STORE")
                        .requestMatchers(HttpMethod.GET, "/api/products/me").hasRole("STORE")
                        .requestMatchers(HttpMethod.GET, "/api/products/me/dashboard").hasRole("STORE")
                        .requestMatchers(HttpMethod.PUT, "/api/products/*").hasRole("STORE")
                        .requestMatchers(HttpMethod.PATCH, "/api/products/*/deactivate").hasRole("STORE")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/*").hasRole("STORE")
                        .requestMatchers(HttpMethod.POST, "/api/affiliate-links").hasRole("AFFILIATE")
                        .requestMatchers(HttpMethod.GET, "/api/affiliate-links/me/dashboard").hasRole("AFFILIATE")
                        .requestMatchers(HttpMethod.POST, "/api/hirings").hasRole("STORE")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(accessTokenJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }
}
