package com.doeja.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.doeja.dto.CentroDoacaoMapper;
import com.doeja.dto.CentroDoacaoRequestDTO;
import com.doeja.dto.CentroDoacaoResponseDTO;
import com.doeja.entity.CentroDoacao;
import com.doeja.exception.ResourceNotFoundException;
import com.doeja.repository.CentroDoacaoRepository;

@Service
public class CentroDoacaoService {

    private final CentroDoacaoRepository repository;

    public CentroDoacaoService(CentroDoacaoRepository repository) {
        this.repository = repository;
    }

    public List<CentroDoacaoResponseDTO> listarTodos() {
        return repository.findAll()
                .stream()
                .map(CentroDoacaoMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CentroDoacaoResponseDTO buscarPorId(Long id) {
        CentroDoacao centro = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de doação não encontrado com id: " + id));

        return CentroDoacaoMapper.toResponseDTO(centro);
    }

    public CentroDoacaoResponseDTO salvar(CentroDoacaoRequestDTO dto) {
        CentroDoacao centro = CentroDoacaoMapper.toEntity(dto);
        CentroDoacao salvo = repository.save(centro);
        return CentroDoacaoMapper.toResponseDTO(salvo);
    }

    public CentroDoacaoResponseDTO atualizar(Long id, CentroDoacaoRequestDTO dto) {
        CentroDoacao centro = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de doação não encontrado com id: " + id));

        centro.setNome(dto.getNome());
        centro.setDescricao(dto.getDescricao());
        centro.setEndereco(dto.getEndereco());
        centro.setBairro(dto.getBairro());
        centro.setCidade(dto.getCidade());
        centro.setEstado(dto.getEstado());
        centro.setCep(dto.getCep());
        centro.setTelefone(dto.getTelefone());
        centro.setHorarioFuncionamento(dto.getHorarioFuncionamento());
        centro.setLatitude(dto.getLatitude());
        centro.setLongitude(dto.getLongitude());
        centro.setAtivo(dto.getAtivo());

        CentroDoacao atualizado = repository.save(centro);
        return CentroDoacaoMapper.toResponseDTO(atualizado);
    }

    public void deletar(Long id) {
        CentroDoacao centro = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de doação não encontrado com id: " + id));

        repository.delete(centro);
    }
}