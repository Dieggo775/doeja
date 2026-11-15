package com.doeja.dto;

import java.util.List;
import java.util.Locale;

import com.doeja.entity.CentroDoacao;
import com.doeja.entity.NecessidadeCentro;

public final class CentroDoacaoMapper {

    private CentroDoacaoMapper() {
    }

    /** Copia os campos editáveis do pedido para a entidade (cadastro e edição). */
    public static void copiar(CentroDoacaoRequestDTO dto, CentroDoacao centro) {
        centro.setNome(dto.nome().trim());
        centro.setDescricao(vazioParaNulo(dto.descricao()));
        centro.setEndereco(dto.endereco().trim());
        centro.setBairro(dto.bairro().trim());
        centro.setCidade(dto.cidade().trim());
        centro.setEstado(dto.estado().trim().toUpperCase(Locale.ROOT));
        centro.setCep(formatarCep(dto.cep()));
        centro.setTelefone(dto.telefone().trim());
        centro.setWhatsapp(vazioParaNulo(dto.whatsapp()));
        centro.setHorarioFuncionamento(dto.horarioFuncionamento().trim());
        centro.setLatitude(dto.latitude());
        centro.setLongitude(dto.longitude());
    }

    public static CentroDoacaoResponseDTO toResponseDTO(CentroDoacao centro, List<NecessidadeDTO> necessidades) {
        return toResponseDTO(centro, null, necessidades);
    }

    public static CentroDoacaoResponseDTO toResponseDTO(CentroDoacao centro, Double distanciaKm,
                                                        List<NecessidadeDTO> necessidades) {
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
                centro.getWhatsapp(),
                centro.getHorarioFuncionamento(),
                centro.getLatitude(),
                centro.getLongitude(),
                centro.getAtivo(),
                distanciaKm,
                centro.getStatus(),
                necessidades == null ? List.of() : necessidades
        );
    }

    public static NecessidadeDTO toNecessidadeDTO(NecessidadeCentro n) {
        return new NecessidadeDTO(
                n.getCategoria().getSlug(),
                n.getCategoria().getNome(),
                n.getTipo(),
                n.getDescricao(),
                n.getUrgente());
    }

    private static String vazioParaNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    /** Guarda o CEP sempre como 00000-000. */
    static String formatarCep(String cep) {
        if (cep == null || cep.isBlank()) {
            return null;
        }
        String digitos = cep.replaceAll("\\D", "");
        return digitos.length() == 8 ? digitos.substring(0, 5) + "-" + digitos.substring(5) : cep.trim();
    }
}
