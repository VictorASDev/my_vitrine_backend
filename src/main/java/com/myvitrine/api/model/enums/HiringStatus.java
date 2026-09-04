package com.myvitrine.api.model.enums;

/**
 * Ciclo de vida de uma contratacao de criador de conteudo por um lojista.
 * Transicoes validas (aplicadas em HiringService):
 * REQUESTED -> ACCEPTED -> IN_PRODUCTION -> DELIVERED -> APPROVED
 */
public enum HiringStatus {
    REQUESTED,
    REJECTED,
    ACCEPTED,
    IN_PRODUCTION,
    DELIVERED,
    APPROVED
}
