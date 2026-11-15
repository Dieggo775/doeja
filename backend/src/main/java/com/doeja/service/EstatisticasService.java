package com.doeja.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doeja.dto.EstatisticasDTO;
import com.doeja.dto.EstatisticasDTO.Contagem;
import com.doeja.entity.Papel;
import com.doeja.entity.StatusCentro;
import com.doeja.entity.StatusDoacao;
import com.doeja.repository.CentroDoacaoRepository;
import com.doeja.repository.DoacaoRepository;
import com.doeja.repository.NecessidadeCentroRepository;
import com.doeja.repository.UsuarioRepository;

/**
 * Indicadores para o painel de impacto (e para o relatório do projeto).
 * Calculados direto do banco: refletem exatamente o uso real do sistema.
 */
@Service
@Transactional(readOnly = true)
public class EstatisticasService {

    private final CentroDoacaoRepository centros;
    private final DoacaoRepository doacoes;
    private final NecessidadeCentroRepository necessidades;
    private final UsuarioRepository usuarios;

    public EstatisticasService(CentroDoacaoRepository centros, DoacaoRepository doacoes,
                               NecessidadeCentroRepository necessidades, UsuarioRepository usuarios) {
        this.centros = centros;
        this.doacoes = doacoes;
        this.necessidades = necessidades;
        this.usuarios = usuarios;
    }

    public EstatisticasDTO calcular() {
        Map<StatusDoacao, Long> contagem = new EnumMap<>(StatusDoacao.class);
        for (StatusDoacao s : StatusDoacao.values()) {
            contagem.put(s, doacoes.countByStatus(s));
        }
        Map<String, Long> porStatus = new LinkedHashMap<>();
        contagem.forEach((s, n) -> porStatus.put(s.name(), n));

        long total = contagem.values().stream().mapToLong(Long::longValue).sum();
        long canceladas = contagem.get(StatusDoacao.CANCELADA);
        long confirmadas = contagem.get(StatusDoacao.CONFIRMADA);

        List<Object[]> confirmadasDatas = doacoes.datasDasConfirmadas();
        List<Contagem> itensPorCategoria = paraContagens(doacoes.somarItensConfirmadosPorCategoria());
        List<Contagem> centrosPorBairro = paraContagens(centros.contarAprovadosPorBairro());

        return new EstatisticasDTO(
                centros.countByStatusAndAtivoTrue(StatusCentro.APROVADO),
                centros.countByStatus(StatusCentro.PENDENTE),
                centrosPorBairro.size(),
                necessidades.contarUrgentesPublicas(),
                usuarios.countByPapel(Papel.USUARIO),
                total,
                porStatus,
                taxaConfirmacao(confirmadas, total - canceladas),
                itensPorCategoria.stream().mapToLong(Contagem::total).sum(),
                tempoMedioHoras(confirmadasDatas),
                tempoMedioMinutos(confirmadasDatas),
                itensPorCategoria,
                centrosPorBairro);
    }

    /** % das doações não canceladas que chegaram a ser confirmadas pelo centro. */
    static double taxaConfirmacao(long confirmadas, long validas) {
        if (validas <= 0) {
            return 0.0;
        }
        return Math.round(confirmadas * 1000.0 / validas) / 10.0;
    }

    /** Tempo médio (horas, 1 casa) entre registrar a doação e o centro confirmar. Null se não houver dados. */
    static Double tempoMedioHoras(List<Object[]> pares) {
        Long minutos = tempoMedioMinutos(pares);
        return minutos == null ? null : Math.round(minutos / 6.0) / 10.0;
    }

    /** O mesmo tempo médio, em minutos inteiros. Null se não houver dados. */
    static Long tempoMedioMinutos(List<Object[]> pares) {
        if (pares.isEmpty()) {
            return null;
        }
        double media = pares.stream()
                .mapToLong(p -> Duration.between((OffsetDateTime) p[0], (OffsetDateTime) p[1]).toSeconds())
                .average()
                .orElse(0);
        return Math.round(media / 60.0);
    }

    private static List<Contagem> paraContagens(List<Object[]> linhas) {
        return linhas.stream()
                .map(l -> new Contagem(l[0] == null ? "Não informado" : l[0].toString(), ((Number) l[1]).longValue()))
                .toList();
    }
}
