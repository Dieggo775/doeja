package com.doeja.entity;

/**
 * Ciclo de vida de uma doação:
 * PROMETIDA -> ENTREGUE (doador avisa que levou) -> CONFIRMADA (centro confirma o recebimento).
 * PROMETIDA pode virar CANCELADA (pelo doador ou pelo centro).
 */
public enum StatusDoacao {
    PROMETIDA,
    ENTREGUE,
    CONFIRMADA,
    CANCELADA
}
