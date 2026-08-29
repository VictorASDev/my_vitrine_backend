package com.myvitrine.api.exception;

/**
 * Lancada quando um refresh token esta ausente, malformado, com assinatura
 * invalida, expirado, revogado ou nao encontrado. Mapeada para HTTP 401.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
