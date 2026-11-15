package com.doeja.dto;

import java.util.List;
import java.util.Map;

/**
 * Indicadores de impacto (painel público). Só números agregados, nenhum dado pessoal.
 */
public record EstatisticasDTO(
        long centrosAtivos,
        long centrosPendentes,
        long bairrosAtendidos,
        long necessidadesUrgentes,
        long doadoresCadastrados,
        long doacoesRegistradas,
        Map<String, Long> doacoesPorStatus,
        double taxaConfirmacaoPercentual,
        long itensConfirmados,
        Double tempoMedioConfirmacaoHoras,
        Long tempoMedioConfirmacaoMinutos,
        List<Contagem> itensPorCategoria,
        List<Contagem> centrosPorBairro
) {
    public record Contagem(String nome, long total) {
    }
}
