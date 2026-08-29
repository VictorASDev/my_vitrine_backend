package com.myvitrine.api.model.enums;

/**
 * Status de um pagamento pendente de liberacao na plataforma.
 * Reutilizado por Commission (comissao de afiliado) e CreatorFee (cache de
 * criador), que compartilham o mesmo ciclo de vida: PENDING ate a
 * confirmacao, CONFIRMED apos liberado.
 */
public enum PaymentStatus {
    PENDING,
    CONFIRMED
}
