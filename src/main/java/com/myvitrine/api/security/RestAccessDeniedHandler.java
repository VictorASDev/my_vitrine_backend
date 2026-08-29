package com.myvitrine.api.security;

import tools.jackson.databind.ObjectMapper;
import com.myvitrine.api.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Formata em JSON (ApiErrorResponse) as rejeicoes de autorizacao decididas
 * pelo proprio filtro de seguranca (ex.: regra hasRole(...) de
 * authorizeHttpRequests). AccessDeniedException lancada manualmente dentro
 * de um Controller (ex.: checagem de dono do recurso) e tratada por
 * GlobalExceptionHandler, nao por aqui.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {


    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        ApiErrorResponse body = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Voce nao tem permissao para executar esta acao", request.getRequestURI());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
