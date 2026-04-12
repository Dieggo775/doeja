package com.doeja.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.doeja.entity.CentroDoacao;
import com.doeja.repository.CentroDoacaoRepository;

@Service
public class CentroDoacaoService {

    private final CentroDoacaoRepository repository;

    public CentroDoacaoService(CentroDoacaoRepository repository) {
        this.repository = repository;
    }

    public Page<CentroDoacao> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<CentroDoacao> listarFiltrado(String bairro, String cidade, Boolean ativo, String cep, Pageable pageable) {
        if (bairro != null && !bairro.trim().isEmpty()) {
            return repository.findAllByBairroContainingIgnoreCase(bairro.trim(), pageable);
        }

        if (cidade != null && !cidade.trim().isEmpty()) {
            return repository.findAllByCidadeContainingIgnoreCase(cidade.trim(), pageable);
        }

        if (ativo != null) {
            return repository.findAllByAtivo(ativo, pageable);
        }

        if (cep != null && !cep.trim().isEmpty()) {
            return repository.findAllByCepContaining(cep.trim(), pageable);
        }

        return repository.findAll(pageable);
    }
}