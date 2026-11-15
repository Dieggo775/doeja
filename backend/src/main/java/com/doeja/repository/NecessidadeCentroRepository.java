package com.doeja.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doeja.entity.NecessidadeCentro;

@Repository
public interface NecessidadeCentroRepository extends JpaRepository<NecessidadeCentro, Long> {

    /** Necessidades ativas de vários centros de uma vez (evita uma consulta por card). */
    @Query("""
            select n from NecessidadeCentro n
            join fetch n.categoria
            where n.centro.id in :centroIds and n.ativa = true
            order by n.urgente desc, n.categoria.ordem asc
            """)
    List<NecessidadeCentro> buscarAtivasDosCentros(@Param("centroIds") Collection<Long> centroIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from NecessidadeCentro n where n.centro.id = :centroId")
    void apagarDoCentro(@Param("centroId") Long centroId);

    @Query("""
            select count(n) from NecessidadeCentro n
            where n.ativa = true and n.urgente = true
              and n.centro.status = com.doeja.entity.StatusCentro.APROVADO and n.centro.ativo = true
            """)
    long contarUrgentesPublicas();
}
