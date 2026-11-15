package com.doeja.exceptions;

/** Conflito com o estado atual (ex.: e-mail já cadastrado). Vira HTTP 409. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String message) {
        super(message);
    }
}
