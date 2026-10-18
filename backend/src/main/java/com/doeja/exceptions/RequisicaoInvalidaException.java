package com.doeja.exceptions;

/** Parâmetro de entrada inválido (vira resposta HTTP 400). */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String message) {
        super(message);
    }
}
