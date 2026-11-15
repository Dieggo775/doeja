package com.doeja.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.doeja.dto.DoacaoDTOs.DoacaoRequest;
import com.doeja.dto.DoacaoDTOs.DoacaoResponse;
import com.doeja.dto.DoacaoDTOs.StatusRequest;
import com.doeja.security.UsuarioLogado;
import com.doeja.service.DoacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doacoes")
public class DoacaoController {

    private final DoacaoService service;

    public DoacaoController(DoacaoService service) {
        this.service = service;
    }

    /** Doador (perfil USUARIO) registra uma doação para um centro. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoacaoResponse registrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DoacaoRequest req) {
        return service.registrar(UsuarioLogado.id(jwt), req);
    }

    /** Doações feitas pelo usuário logado. */
    @GetMapping("/minhas")
    public List<DoacaoResponse> minhas(@AuthenticationPrincipal Jwt jwt) {
        return service.minhas(UsuarioLogado.id(jwt));
    }

    /** Doações recebidas pelos centros do usuário logado (perfil CENTRO). */
    @GetMapping("/recebidas")
    public List<DoacaoResponse> recebidas(@AuthenticationPrincipal Jwt jwt) {
        return service.recebidas(UsuarioLogado.id(jwt));
    }

    @PatchMapping("/{id}/status")
    public DoacaoResponse alterarStatus(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long id,
                                        @Valid @RequestBody StatusRequest req) {
        return service.alterarStatus(UsuarioLogado.id(jwt), id, req.status());
    }
}
