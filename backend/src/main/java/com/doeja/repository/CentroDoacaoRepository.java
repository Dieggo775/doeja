package com.doeja.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.doeja.entity.CentroDoacao;

public interface CentroDoacaoRepository extends JpaRepository<CentroDoacao, Long> {
    List<CentroDoacao> findByCidadeIgnoreCase(String cidade);
    List<CentroDoacao> findByBairroIgnoreCase(String bairro);

    @Query("SELECT c FROM CentroDoacao c WHERE " +
           "(:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
           "(:cidade IS NULL OR LOWER(c.cidade) LIKE LOWER(CONCAT('%', :cidade, '%'))) AND " +
           "(:bairro IS NULL OR LOWER(c.bairro) LIKE LOWER(CONCAT('%', :bairro, '%'))) AND " +
           "(:ativo IS NULL OR c.ativo = :ativo)")
    Page<CentroDoacao> findByFiltros(@Param("nome") String nome,
                                     @Param("cidade") String cidade,
                                     @Param("bairro") String bairro,
                                     @Param("ativo") Boolean ativo,
                                     Pageable pageable);
}