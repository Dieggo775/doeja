package com.doeja.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.doeja.dto.CentroDoacaoRequestDTO;
import com.doeja.dto.CentroDoacaoResponseDTO;
import com.doeja.dto.NecessidadeDTO;
import com.doeja.exceptions.RequisicaoInvalidaException;
import com.doeja.security.UsuarioLogado;
import com.doeja.service.CentroDoacaoService;

import jakarta.validation.Valid;

/** Área do responsável por centros (perfil CENTRO). Cada um só enxerga e altera os próprios. */
@RestController
@RequestMapping("/api/meus-centros")
public class MeusCentrosController {

    private final CentroDoacaoService service;

    public MeusCentrosController(CentroDoacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<CentroDoacaoResponseDTO> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listarDoResponsavel(UsuarioLogado.id(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CentroDoacaoResponseDTO cadastrar(@AuthenticationPrincipal Jwt jwt,
                                             @Valid @RequestBody CentroDoacaoRequestDTO req) {
        return service.cadastrar(UsuarioLogado.id(jwt), req);
    }

    @PutMapping("/{id}")
    public CentroDoacaoResponseDTO atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id,
                                             @Valid @RequestBody CentroDoacaoRequestDTO req) {
        return service.atualizar(UsuarioLogado.id(jwt), id, req);
    }

    /** Corpo: {"ativo": true|false} — pausar ou retomar o recebimento de doações. */
    @PatchMapping("/{id}/ativo")
    public CentroDoacaoResponseDTO alterarAtivo(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id,
                                                @RequestBody Map<String, Boolean> corpo) {
        Boolean ativo = corpo == null ? null : corpo.get("ativo");
        if (ativo == null) {
            throw new RequisicaoInvalidaException("Informe \"ativo\": true ou false.");
        }
        return service.alterarAtivo(UsuarioLogado.id(jwt), id, ativo);
    }

    @PutMapping("/{id}/necessidades")
    public CentroDoacaoResponseDTO necessidades(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id,
                                                @RequestBody List<NecessidadeDTO> lista) {
        return service.substituirNecessidades(UsuarioLogado.id(jwt), id, lista);
    }
}
