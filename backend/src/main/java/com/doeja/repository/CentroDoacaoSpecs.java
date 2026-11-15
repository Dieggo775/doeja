package com.doeja.repository;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.doeja.entity.CentroDoacao;
import com.doeja.entity.NecessidadeCentro;
import com.doeja.entity.StatusCentro;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

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

    /** O centro tem uma necessidade ativa na categoria (slug), ex.: "roupas". */
    public static Specification<CentroDoacao> temNecessidade(String slugCategoria) {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<NecessidadeCentro> n = sub.from(NecessidadeCentro.class);
            sub.select(n.get("id")).where(
                    cb.equal(n.get("centro"), root),
                    cb.equal(n.get("ativa"), true),
                    cb.equal(n.get("categoria").get("slug"), slugCategoria));
            return cb.exists(sub);
        };
    }

    /** Impede que %, _ e \ digitados pelo usuário virem curingas do LIKE. */
    static String escaparLike(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
