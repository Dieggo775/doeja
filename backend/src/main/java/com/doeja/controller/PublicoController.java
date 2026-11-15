package com.doeja.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doeja.dto.CategoriaDTO;
import com.doeja.dto.EstatisticasDTO;
import com.doeja.service.CentroDoacaoService;
import com.doeja.service.EstatisticasService;

/** Dados públicos de apoio: categorias, painel de impacto e informações do ambiente. */
@RestController
@RequestMapping("/api")
public class PublicoController {

    private final CentroDoacaoService centros;
    private final EstatisticasService estatisticas;
    private final boolean demonstracao;

    public PublicoController(CentroDoacaoService centros, EstatisticasService estatisticas,
                             @Value("${doeja.demo.enabled:false}") boolean demonstracao) {
        this.centros = centros;
        this.estatisticas = estatisticas;
        this.demonstracao = demonstracao;
    }

    @GetMapping("/categorias")
    public List<CategoriaDTO> categorias() {
        return centros.listarCategorias();
    }

    @GetMapping("/estatisticas")
    public EstatisticasDTO estatisticas() {
        return estatisticas.calcular();
    }

    /** O front usa "demonstracao" para avisar que as instituições exibidas são fictícias. */
    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of("demonstracao", demonstracao);
    }
}
