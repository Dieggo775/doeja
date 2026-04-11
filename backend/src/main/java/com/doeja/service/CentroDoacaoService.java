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
}