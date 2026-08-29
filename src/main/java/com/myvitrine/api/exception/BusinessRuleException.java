package com.myvitrine.api.exception;

/**
 * Lancada quando uma regra de negocio do dominio e violada (ex.: transicao
 * de status invalida, produto inativo). Mapeada para HTTP 400.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
