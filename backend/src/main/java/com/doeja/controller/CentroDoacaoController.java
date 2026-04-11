package com.doeja.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doeja.dto.CentroDoacaoFiltroDTO;
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
    public ResponseEntity<Page<CentroDoacaoResponseDTO>> listarTodos(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "nome,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort sortObj = Sort.by(Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(service.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroDoacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/filtro")
    public ResponseEntity<Page<CentroDoacaoResponseDTO>> filtrar(
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String cidade,
        @RequestParam(required = false) String bairro,
        @RequestParam(required = false) Boolean ativo,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "nome,asc") String sort) {
        
        String[] sortParams = sort.split(",");
        Sort sortObj = Sort.by(Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        CentroDoacaoFiltroDTO filtro = new CentroDoacaoFiltroDTO();
        filtro.setNome(nome);
        filtro.setCidade(cidade);
        filtro.setBairro(bairro);
        filtro.setAtivo(ativo);
        
        return ResponseEntity.ok(service.filtrar(filtro, pageable));
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