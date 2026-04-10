package com.doeja.repository;

import com.doeja.entity.CentroDoacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CentroDoacaoRepository extends JpaRepository<CentroDoacao, Long> {
    List<CentroDoacao> findByCidadeIgnoreCase(String cidade);
    List<CentroDoacao> findByBairroIgnoreCase(String bairro);
}