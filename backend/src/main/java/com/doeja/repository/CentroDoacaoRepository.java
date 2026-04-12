package com.doeja.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.doeja.entity.CentroDoacao;

@Repository
public interface CentroDoacaoRepository extends JpaRepository<CentroDoacao, Long> {

    Page<CentroDoacao> findAllByBairroContainingIgnoreCase(String bairro, Pageable pageable);

    Page<CentroDoacao> findAllByCidadeContainingIgnoreCase(String cidade, Pageable pageable);

    Page<CentroDoacao> findAllByAtivo(Boolean ativo, Pageable pageable);

    Page<CentroDoacao> findAllByCepContaining(String cep, Pageable pageable);
}