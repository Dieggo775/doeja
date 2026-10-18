package com.doeja.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * O que a API devolve para o público sobre um centro.
 * "distanciaKm" só aparece na busca por proximidade.
 */
public record CentroDoacaoResponseDTO(
        Long id,
        String nome,
        String descricao,
        String endereco,
        String bairro,
        String cidade,
        String estado,
        String cep,
        String telefone,
        String whatsapp,
        String horarioFuncionamento,
        Double latitude,
        Double longitude,
        Boolean ativo,
        @JsonInclude(JsonInclude.Include.NON_NULL) Double distanciaKm
) {
}
