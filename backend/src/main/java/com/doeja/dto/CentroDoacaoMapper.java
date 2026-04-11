package com.doeja.dto;

import com.doeja.entity.CentroDoacao;

public class CentroDoacaoMapper {

    public static CentroDoacao toEntity(CentroDoacaoRequestDTO dto) {
        CentroDoacao centro = new CentroDoacao();
        centro.setNome(dto.getNome());
        centro.setDescricao(dto.getDescricao());
        centro.setEndereco(dto.getEndereco());
        centro.setBairro(dto.getBairro());
        centro.setCidade(dto.getCidade());
        centro.setEstado(dto.getEstado());
        centro.setCep(dto.getCep());
        centro.setTelefone(dto.getTelefone());
        centro.setHorarioFuncionamento(dto.getHorarioFuncionamento());
        centro.setLatitude(dto.getLatitude());
        centro.setLongitude(dto.getLongitude());
        centro.setAtivo(dto.getAtivo());
        return centro;
    }

    public static CentroDoacaoResponseDTO toResponseDTO(CentroDoacao centro) {
        return new CentroDoacaoResponseDTO(
                centro.getId(),
                centro.getNome(),
                centro.getDescricao(),
                centro.getEndereco(),
                centro.getBairro(),
                centro.getCidade(),
                centro.getEstado(),
                centro.getCep(),
                centro.getTelefone(),
                centro.getHorarioFuncionamento(),
                centro.getLatitude(),
                centro.getLongitude(),
                centro.getAtivo()
        );
    }
}