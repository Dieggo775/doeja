package com.doeja.dto;

import com.doeja.entity.TipoNecessidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * O que um centro precisa/aceita. Usado na resposta e no pedido de atualização
 * (no pedido, "categoriaNome" é ignorado: vale o "categoria" = slug).
 */
public record NecessidadeDTO(
        @NotBlank(message = "Informe a categoria.")
        String categoria,

        String categoriaNome,

        @NotNull(message = "Informe o tipo (PRECISA ou ACEITA).")
        TipoNecessidade tipo,

        @Size(max = 300, message = "A descrição pode ter até 300 caracteres.")
        String descricao,

        Boolean urgente
) {
}
