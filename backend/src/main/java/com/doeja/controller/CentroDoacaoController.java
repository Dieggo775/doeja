package com.doeja.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doeja.dto.CentroDoacaoResponseDTO;
import com.doeja.service.CentroDoacaoService;

@RestController
@RequestMapping("/api/centros")
public class CentroDoacaoController {

    private final CentroDoacaoService service;

    public CentroDoacaoController(CentroDoacaoService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CentroDoacaoResponseDTO> listar(
            @RequestParam(name = "bairro", required = false) String bairro,
            @RequestParam(name = "cidade", required = false) String cidade,
            @RequestParam(name = "cep", required = false) String cep,
            @RequestParam(name = "ativo", required = false) Boolean ativo,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return service.listar(bairro, cidade, cep, ativo, pageable);
    }

    @GetMapping("/proximos")
    public List<CentroDoacaoResponseDTO> proximos(
            @RequestParam(name = "lat") double lat,
            @RequestParam(name = "lng") double lng,
            @RequestParam(name = "raioKm", defaultValue = "5") double raioKm,
            @RequestParam(name = "limite", defaultValue = "20") int limite
    ) {
        return service.listarProximos(lat, lng, raioKm, limite);
    }

    @GetMapping("/{id}")
    public CentroDoacaoResponseDTO buscar(@PathVariable("id") Long id) {
        return service.buscarPorId(id);
    }
}
