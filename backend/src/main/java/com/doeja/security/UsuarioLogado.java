package com.doeja.security;

import org.springframework.security.oauth2.jwt.Jwt;

import com.doeja.exceptions.NaoAutorizadoException;

/** Lê o id do usuário de dentro do token já validado. */
public final class UsuarioLogado {

    private UsuarioLogado() {
    }

    public static Long id(Jwt jwt) {
        if (jwt == null) {
            throw new NaoAutorizadoException("Faça login para continuar.");
        }
        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException e) {
            throw new NaoAutorizadoException("Token inválido.");
        }
    }
}
