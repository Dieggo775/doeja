package com.doeja.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.doeja.entity.Usuario;

/** Gera os tokens JWT entregues no login. */
@Service
public class JwtService {

    public static final String CLAIM_PAPEL = "papel";
    public static final String CLAIM_NOME = "nome";

    private final JwtEncoder encoder;
    private final Duration validade;

    public JwtService(JwtEncoder encoder, @Value("${doeja.jwt.validade-horas:8}") long validadeHoras) {
        this.encoder = encoder;
        this.validade = Duration.ofHours(validadeHoras);
    }

    public record Token(String valor, Instant expiraEm) {
    }

    public Token gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expira = agora.plus(validade);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("doeja")
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(agora)
                .expiresAt(expira)
                .claim(CLAIM_PAPEL, usuario.getPapel().name())
                .claim(CLAIM_NOME, usuario.getNome())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Token(valor, expira);
    }
}
