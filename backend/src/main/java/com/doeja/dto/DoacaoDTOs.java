package com.doeja.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.doeja.entity.StatusDoacao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Pedidos e respostas de doações. */
public final class DoacaoDTOs {

    private DoacaoDTOs() {
    }

    public record ItemRequest(
            @NotBlank(message = "Informe a categoria do item.")
            String categoria,

            @NotBlank(message = "Descreva o item.")
            @Size(max = 200, message = "A descrição do item pode ter até 200 caracteres.")
            String descricao,

            @NotNull(message = "Informe a quantidade.")
            @Min(value = 1, message = "A quantidade mínima é 1.")
            @Max(value = 1000, message = "A quantidade máxima por item é 1000.")
            Integer quantidade
    ) {
    }

    public record DoacaoRequest(
            @NotNull(message = "Informe o centro.")
            Long centroId,

            @Size(max = 500, message = "A observação pode ter até 500 caracteres.")
            String observacao,

            @NotEmpty(message = "Inclua pelo menos um item.")
            @Size(max = 20, message = "No máximo 20 itens por doação.")
            List<@Valid ItemRequest> itens
    ) {
    }

    public record StatusRequest(
            @NotNull(message = "Informe o novo status.")
            StatusDoacao status
    ) {
    }

    public record ItemResponse(String categoria, String categoriaNome, String descricao, Integer quantidade) {
    }

    /**
     * Visão da doação. "doadorNome" aparece para o centro que recebe;
     * e-mail e outros dados pessoais do doador nunca são expostos (LGPD).
     */
    public record DoacaoResponse(
            Long id,
            Long centroId,
            String centroNome,
            String centroEndereco,
            String doadorNome,
            StatusDoacao status,
            String observacao,
            OffsetDateTime criadoEm,
            OffsetDateTime atualizadoEm,
            List<ItemResponse> itens
    ) {
    }
}
