package com.doeja.exceptions;

/** Credenciais inválidas ou login bloqueado temporariamente. Vira HTTP 401 (ou 429 se bloqueado). */
public class NaoAutorizadoException extends RuntimeException {

    private final boolean bloqueado;

    public NaoAutorizadoException(String message) {
        this(message, false);
    }

    public NaoAutorizadoException(String message, boolean bloqueado) {
        super(message);
        this.bloqueado = bloqueado;
    }

    public boolean isBloqueado() {
        return bloqueado;
    }
}
