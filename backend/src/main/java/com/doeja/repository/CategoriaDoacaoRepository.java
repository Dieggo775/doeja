package com.doeja.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.doeja.entity.CategoriaDoacao;

@Repository
public interface CategoriaDoacaoRepository extends JpaRepository<CategoriaDoacao, Long> {

    Optional<CategoriaDoacao> findBySlug(String slug);

    List<CategoriaDoacao> findAllByOrderByOrdemAscNomeAsc();
}
