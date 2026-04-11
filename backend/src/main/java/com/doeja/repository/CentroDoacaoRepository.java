package com.doeja.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.doeja.entity.CentroDoacao;

@Repository
public interface CentroDoacaoRepository extends JpaRepository<CentroDoacao, Long> {
}