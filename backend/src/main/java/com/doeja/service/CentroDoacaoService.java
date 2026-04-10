package com.doeja.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.doeja.entity.CentroDoacao;
import com.doeja.repository.CentroDoacaoRepository;

@Service
public class CentroDoacaoService {

    private final CentroDoacaoRepository repository;

    public CentroDoacaoService(CentroDoacaoRepository repository) {
        this.repository = repository;
    }

    public List<CentroDoacao> listarTodos() {
        return repository.findAll();
    }

    public Optional<CentroDoacao> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<CentroDoacao> buscarPorCidade(String cidade) {
        return repository.findByCidadeIgnoreCase(cidade);
    }

    public List<CentroDoacao> buscarPorBairro(String bairro) {
        return repository.findByBairroIgnoreCase(bairro);
    }

    public CentroDoacao salvar(CentroDoacao centro) {
        return repository.save(centro);
    }

    public CentroDoacao atualizar(Long id, CentroDoacao dadosAtualizados) {
        CentroDoacao centro = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Centro não encontrado"));

        centro.setNome(dadosAtualizados.getNome());
        centro.setDescricao(dadosAtualizados.getDescricao());
        centro.setEndereco(dadosAtualizados.getEndereco());
        centro.setBairro(dadosAtualizados.getBairro());
        centro.setCidade(dadosAtualizados.getCidade());
        centro.setEstado(dadosAtualizados.getEstado());
        centro.setCep(dadosAtualizados.getCep());
        centro.setLatitude(dadosAtualizados.getLatitude());
        centro.setLongitude(dadosAtualizados.getLongitude());
        centro.setTelefone(dadosAtualizados.getTelefone());
        centro.setEmail(dadosAtualizados.getEmail());
        centro.setHorarioFuncionamento(dadosAtualizados.getHorarioFuncionamento());
        centro.setAtivo(dadosAtualizados.getAtivo());

        return repository.save(centro);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}