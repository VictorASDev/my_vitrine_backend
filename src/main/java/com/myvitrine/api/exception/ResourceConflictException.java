package com.myvitrine.api.exception;

/**
 * Lancada quando uma operacao viola uma restricao de unicidade ou de estado
 * ja existente (ex.: e-mail duplicado, perfil ja cadastrado). Mapeada para
 * HTTP 409.
 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
