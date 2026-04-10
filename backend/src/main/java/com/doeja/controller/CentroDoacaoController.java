package com.doeja.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doeja.entity.CentroDoacao;
import com.doeja.service.CentroDoacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/centros")
@CrossOrigin(origins = "*")
public class CentroDoacaoController {

    private final CentroDoacaoService service;

    public CentroDoacaoController(CentroDoacaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CentroDoacao>> listar(
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String bairro
    ) {
        if (cidade != null) {
            return ResponseEntity.ok(service.buscarPorCidade(cidade));
        }

        if (bairro != null) {
            return ResponseEntity.ok(service.buscarPorBairro(bairro));
        }

        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroDoacao> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CentroDoacao> criar(@Valid @RequestBody CentroDoacao centro) {
        return ResponseEntity.ok(service.salvar(centro));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CentroDoacao> atualizar(@PathVariable Long id, @RequestBody CentroDoacao centro) {
        return ResponseEntity.ok(service.atualizar(id, centro));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}