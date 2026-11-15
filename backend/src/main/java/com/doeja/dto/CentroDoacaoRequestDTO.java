package com.doeja.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados por um responsável ao cadastrar ou editar um centro.
 * Status e responsável NÃO vêm daqui: são definidos pelo servidor.
 */
public record CentroDoacaoRequestDTO(
        @NotBlank(message = "Informe o nome do centro.")
        @Size(max = 160, message = "O nome pode ter até 160 caracteres.")
        String nome,

        @Size(max = 500, message = "A descrição pode ter até 500 caracteres.")
        String descricao,

        @NotBlank(message = "Informe o endereço.")
        @Size(max = 200, message = "O endereço pode ter até 200 caracteres.")
        String endereco,

        @NotBlank(message = "Informe o bairro.")
        @Size(max = 100)
        String bairro,

        @NotBlank(message = "Informe a cidade.")
        @Size(max = 100)
        String cidade,

        @NotBlank(message = "Informe a UF.")
        @Pattern(regexp = "[A-Za-z]{2}", message = "UF deve ter 2 letras (ex.: SP).")
        String estado,

        @Pattern(regexp = "^$|\\d{5}-?\\d{3}", message = "CEP deve estar no formato 00000-000.")
        String cep,

        @NotBlank(message = "Informe um telefone de contato.")
        @Size(max = 20)
        String telefone,

        @Pattern(regexp = "^$|\\d{10,15}", message = "WhatsApp: só números com DDI e DDD (ex.: 5511999999999).")
        String whatsapp,

        @NotBlank(message = "Informe o horário de funcionamento.")
        @Size(max = 200)
        String horarioFuncionamento,

        @NotNull(message = "Informe a latitude.")
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
        Double latitude,

        @NotNull(message = "Informe a longitude.")
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
        Double longitude
) {
}
