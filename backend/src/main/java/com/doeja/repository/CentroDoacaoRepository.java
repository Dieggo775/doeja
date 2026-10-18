package com.doeja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
}
