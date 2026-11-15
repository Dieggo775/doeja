package com.doeja.dto;

import java.util.List;

import com.doeja.entity.StatusCentro;

/**
 * Centro como a API devolve. "distanciaKm" só vem preenchido na busca por proximidade.
 * "status" interessa ao responsável e ao admin; para o público é sempre APROVADO.
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
        Double distanciaKm,
        StatusCentro status,
        List<NecessidadeDTO> necessidades
) {
}
