package com.doeja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.doeja.entity.CentroDoacao;
import com.doeja.entity.StatusCentro;

@Repository
public interface CentroDoacaoRepository
        extends JpaRepository<CentroDoacao, Long>, JpaSpecificationExecutor<CentroDoacao> {

    /**
     * Pré-filtro da busca por proximidade: centros ativos e aprovados dentro de uma
     * "caixa" de coordenadas. A distância exata é calculada depois, em Java.
     */
    List<CentroDoacao> findByStatusAndAtivoTrueAndLatitudeBetweenAndLongitudeBetween(
            StatusCentro status, Double minLat, Double maxLat, Double minLng, Double maxLng);

    /** Centros administrados por um usuário (perfil CENTRO). */
    List<CentroDoacao> findByResponsavelIdOrderByNomeAsc(Long responsavelId);

    /** Fila de moderação do admin. */
    List<CentroDoacao> findByStatusOrderByCriadoEmAsc(StatusCentro status);

    List<CentroDoacao> findAllByOrderByCriadoEmDesc();

    long countByStatus(StatusCentro status);

    long countByStatusAndAtivoTrue(StatusCentro status);

    /** Quantidade de centros aprovados e ativos por bairro (para o painel de impacto). */
    @Query("""
            select c.bairro, count(c) from CentroDoacao c
            where c.status = com.doeja.entity.StatusCentro.APROVADO and c.ativo = true
            group by c.bairro order by count(c) desc, c.bairro
            """)
    List<Object[]> contarAprovadosPorBairro();
}
