package com.doeja.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Proteção contra tentativa e erro de senha: após MAX_FALHAS erros seguidos para o mesmo
 * e-mail, novas tentativas ficam bloqueadas por BLOQUEIO. Fica em memória (suficiente para
 * uma instância só; com várias instâncias, trocar por Redis ou banco).
 */
@Component
public class LimitadorDeLogin {

    static final int MAX_FALHAS = 5;
    static final Duration BLOQUEIO = Duration.ofMinutes(15);

    private record Situacao(int falhas, Instant bloqueadoAte) {
    }

    private final Map<String, Situacao> porEmail = new ConcurrentHashMap<>();
    private final Clock relogio;

    public LimitadorDeLogin() {
        this(Clock.systemUTC());
    }

    LimitadorDeLogin(Clock relogio) {
        this.relogio = relogio;
    }

    public boolean estaBloqueado(String email) {
        Situacao s = porEmail.get(chave(email));
        return s != null && s.bloqueadoAte() != null && relogio.instant().isBefore(s.bloqueadoAte());
    }

    public void registrarFalha(String email) {
        porEmail.compute(chave(email), (k, atual) -> {
            Instant agora = relogio.instant();
            int falhas = (atual == null || bloqueioVenceu(atual, agora)) ? 1 : atual.falhas() + 1;
            Instant bloqueio = falhas >= MAX_FALHAS ? agora.plus(BLOQUEIO) : null;
            return new Situacao(falhas, bloqueio);
        });
    }

    public void registrarSucesso(String email) {
        porEmail.remove(chave(email));
    }

    private static boolean bloqueioVenceu(Situacao s, Instant agora) {
        return s.bloqueadoAte() != null && !agora.isBefore(s.bloqueadoAte());
    }

    private static String chave(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
