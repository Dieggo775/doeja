package com.doeja.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doeja.dto.CentroDoacaoResponseDTO;
import com.doeja.entity.StatusCentro;
import com.doeja.exceptions.RequisicaoInvalidaException;
import com.doeja.service.CentroDoacaoService;

/** Moderação de cadastros de centros (perfil ADMIN). */
@RestController
@RequestMapping("/api/admin/centros")
public class AdminController {

    private final CentroDoacaoService service;

    public AdminController(CentroDoacaoService service) {
        this.service = service;
    }

    /** Sem "status": todos. Com "status=PENDENTE": fila de aprovação. */
    @GetMapping
    public List<CentroDoacaoResponseDTO> listar(@RequestParam(name = "status", required = false) StatusCentro status) {
        return service.listarParaAdmin(status);
    }

    /** Corpo: {"status": "APROVADO" | "REJEITADO" | "PENDENTE"}. */
    @PatchMapping("/{id}/status")
    public CentroDoacaoResponseDTO moderar(@PathVariable("id") Long id, @RequestBody Map<String, String> corpo) {
        String valor = corpo == null ? null : corpo.get("status");
        StatusCentro status;
        try {
            status = valor == null ? null : StatusCentro.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new RequisicaoInvalidaException("Status inválido: " + valor);
        }
        return service.moderar(id, status);
    }
}
