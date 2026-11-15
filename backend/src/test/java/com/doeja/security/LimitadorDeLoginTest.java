package com.doeja.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class LimitadorDeLoginTest {

    /** Relógio que dá para avançar manualmente. */
    static class RelogioManual extends Clock {
        Instant agora = Instant.parse("2026-11-15T12:00:00Z");

        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
    }

    @Test
    void bloqueiaDepoisDeCincoFalhasSeguidas() {
        LimitadorDeLogin limitador = new LimitadorDeLogin(new RelogioManual());
        for (int i = 0; i < LimitadorDeLogin.MAX_FALHAS - 1; i++) {
            limitador.registrarFalha("a@b.com");
        }
        assertFalse(limitador.estaBloqueado("a@b.com"));

        limitador.registrarFalha("a@b.com");
        assertTrue(limitador.estaBloqueado("a@b.com"));
    }

    @Test
    void emailComMaiusculasContaComoOMesmo() {
        LimitadorDeLogin limitador = new LimitadorDeLogin(new RelogioManual());
        for (int i = 0; i < LimitadorDeLogin.MAX_FALHAS; i++) {
            limitador.registrarFalha(i % 2 == 0 ? "Ana@Exemplo.com" : " ana@exemplo.com ");
        }
        assertTrue(limitador.estaBloqueado("ANA@EXEMPLO.COM"));
    }

    @Test
    void desbloqueiaQuandoOTempoPassa() {
        RelogioManual relogio = new RelogioManual();
        LimitadorDeLogin limitador = new LimitadorDeLogin(relogio);
        for (int i = 0; i < LimitadorDeLogin.MAX_FALHAS; i++) {
            limitador.registrarFalha("x@y.com");
        }
        relogio.agora = relogio.agora.plus(LimitadorDeLogin.BLOQUEIO).plus(Duration.ofSeconds(1));
        assertFalse(limitador.estaBloqueado("x@y.com"));

        // Depois do bloqueio vencido, a contagem recomeça do zero
        limitador.registrarFalha("x@y.com");
        assertFalse(limitador.estaBloqueado("x@y.com"));
    }

    @Test
    void loginCertoZeraAsFalhas() {
        LimitadorDeLogin limitador = new LimitadorDeLogin(new RelogioManual());
        for (int i = 0; i < LimitadorDeLogin.MAX_FALHAS - 1; i++) {
            limitador.registrarFalha("c@d.com");
        }
        limitador.registrarSucesso("c@d.com");
        limitador.registrarFalha("c@d.com");
        assertFalse(limitador.estaBloqueado("c@d.com"));
    }
}
