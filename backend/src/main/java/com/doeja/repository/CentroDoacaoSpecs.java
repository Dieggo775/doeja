package com.doeja.repository;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.doeja.entity.CentroDoacao;
import com.doeja.entity.StatusCentro;

/**
 * Peças de filtro que podem ser combinadas com and(...).
 */
public final class CentroDoacaoSpecs {

    private CentroDoacaoSpecs() {
    }

    public static Specification<CentroDoacao> aprovado() {
        return (root, query, cb) -> cb.equal(root.get("status"), StatusCentro.APROVADO);
    }

    public static Specification<CentroDoacao> ativo(boolean ativo) {
        return (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    /** O campo (bairro, cidade, cep...) contém o texto, sem diferenciar maiúsculas. */
    public static Specification<CentroDoacao> campoContem(String campo, String texto) {
        String padrao = "%" + escaparLike(texto.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.<String>get(campo)), padrao, '\\');
    }

    /** Impede que %, _ e \ digitados pelo usuário virem curingas do LIKE. */
    static String escaparLike(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
