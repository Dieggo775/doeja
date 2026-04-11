package com.doeja.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doeja.entity.CentroDoacao;
import com.doeja.service.CentroDoacaoService;

@RestController
@RequestMapping("/api/centros")
@CrossOrigin(origins = "*")
public class CentroDoacaoController {

    private final CentroDoacaoService service;

    public CentroDoacaoController(CentroDoacaoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CentroDoacao> listar(Pageable pageable) {
        return service.listar(pageable);
    }
}