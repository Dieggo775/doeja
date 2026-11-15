package com.doeja.dto;

import java.time.Instant;

import com.doeja.entity.Papel;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Pedidos e respostas de autenticação. */
public final class AuthDTOs {

    private AuthDTOs() {
    }

    public record CadastroRequest(
            @NotBlank(message = "Informe seu nome.")
            @Size(max = 120, message = "O nome pode ter até 120 caracteres.")
            String nome,

            @NotBlank(message = "Informe seu e-mail.")
            @Email(message = "E-mail inválido.")
            @Size(max = 160)
            String email,

            @NotBlank(message = "Informe uma senha.")
            @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
            String senha,

            /** USUARIO (doador) ou CENTRO (responsável por centro). ADMIN não pode se cadastrar. */
            @NotNull(message = "Informe o tipo de conta.")
            Papel papel,

            @AssertTrue(message = "É preciso aceitar o termo de uso de dados (LGPD).")
            boolean aceiteLgpd
    ) {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe o e-mail.") String email,
            @NotBlank(message = "Informe a senha.") String senha
    ) {
    }

    public record UsuarioDTO(Long id, String nome, String email, Papel papel) {
    }

    public record TokenResponse(String token, Instant expiraEm, UsuarioDTO usuario) {
    }
}
