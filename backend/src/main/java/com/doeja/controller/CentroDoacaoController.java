package com.doeja.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doeja.dto.CentroDoacaoRequestDTO;
import com.doeja.dto.CentroDoacaoResponseDTO;
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
    public ResponseEntity<List<CentroDoacaoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroDoacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CentroDoacaoResponseDTO> criar(@RequestBody CentroDoacaoRequestDTO dto) {
        CentroDoacaoResponseDTO novoCentro = service.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoCentro);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CentroDoacaoResponseDTO> atualizar(@PathVariable Long id,
                                                             @RequestBody CentroDoacaoRequestDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}