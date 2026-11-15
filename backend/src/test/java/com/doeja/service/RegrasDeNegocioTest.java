package com.doeja.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.doeja.entity.StatusDoacao;
import com.doeja.service.DoacaoService.Ator;

/** Regras puras (sem banco): transições de status e cálculo dos indicadores. */
class RegrasDeNegocioTest {

    @Test
    void doadorPodeAvisarEntregaOuCancelarUmaPromessa() {
        assertTrue(DoacaoService.podeMudar(Ator.DOADOR, StatusDoacao.PROMETIDA, StatusDoacao.ENTREGUE));
        assertTrue(DoacaoService.podeMudar(Ator.DOADOR, StatusDoacao.PROMETIDA, StatusDoacao.CANCELADA));
    }

    @Test
    void doadorNaoPodeConfirmarOProprioRecebimento() {
        assertFalse(DoacaoService.podeMudar(Ator.DOADOR, StatusDoacao.PROMETIDA, StatusDoacao.CONFIRMADA));
        assertFalse(DoacaoService.podeMudar(Ator.DOADOR, StatusDoacao.ENTREGUE, StatusDoacao.CONFIRMADA));
    }

    @Test
    void centroConfirmaRecebimento() {
        assertTrue(DoacaoService.podeMudar(Ator.CENTRO, StatusDoacao.ENTREGUE, StatusDoacao.CONFIRMADA));
        assertTrue(DoacaoService.podeMudar(Ator.CENTRO, StatusDoacao.PROMETIDA, StatusDoacao.CONFIRMADA));
    }

    @Test
    void estadosFinaisNaoMudamMais() {
        for (Ator ator : Ator.values()) {
            for (StatusDoacao novo : StatusDoacao.values()) {
                assertFalse(DoacaoService.podeMudar(ator, StatusDoacao.CONFIRMADA, novo));
                assertFalse(DoacaoService.podeMudar(ator, StatusDoacao.CANCELADA, novo));
            }
        }
    }

    @Test
    void taxaDeConfirmacaoIgnoraCanceladasEArredonda() {
        assertEquals(0.0, EstatisticasService.taxaConfirmacao(0, 0));
        assertEquals(66.7, EstatisticasService.taxaConfirmacao(2, 3));
        assertEquals(100.0, EstatisticasService.taxaConfirmacao(4, 4));
    }

    @Test
    void tempoMedioDeConfirmacaoEmHoras() {
        OffsetDateTime t0 = OffsetDateTime.parse("2026-11-10T10:00:00-03:00");
        List<Object[]> pares = List.of(
                new Object[] { t0, t0.plusHours(2) },
                new Object[] { t0, t0.plusHours(4) });
        assertEquals(3.0, EstatisticasService.tempoMedioHoras(pares));
        assertEquals(180L, EstatisticasService.tempoMedioMinutos(pares));
        assertNull(EstatisticasService.tempoMedioHoras(List.of()));
        assertNull(EstatisticasService.tempoMedioMinutos(List.of()));
    }
}
