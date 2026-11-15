package com.doeja.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doeja.dto.DoacaoDTOs.DoacaoRequest;
import com.doeja.dto.DoacaoDTOs.DoacaoResponse;
import com.doeja.dto.DoacaoDTOs.ItemRequest;
import com.doeja.dto.DoacaoDTOs.ItemResponse;
import com.doeja.entity.CategoriaDoacao;
import com.doeja.entity.CentroDoacao;
import com.doeja.entity.Doacao;
import com.doeja.entity.ItemDoacao;
import com.doeja.entity.StatusCentro;
import com.doeja.entity.StatusDoacao;
import com.doeja.entity.Usuario;
import com.doeja.exceptions.ConflitoException;
import com.doeja.exceptions.RequisicaoInvalidaException;
import com.doeja.exceptions.ResourceNotFoundException;
import com.doeja.repository.CategoriaDoacaoRepository;
import com.doeja.repository.CentroDoacaoRepository;
import com.doeja.repository.DoacaoRepository;
import com.doeja.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class DoacaoService {

    /** Quem está mudando o status de uma doação. */
    public enum Ator { DOADOR, CENTRO }

    /**
     * Transições permitidas, por ator. Tudo que não está aqui é recusado.
     * Doador: avisa que entregou ou desiste. Centro: confirma o recebimento ou cancela.
     */
    static final Map<Ator, Map<StatusDoacao, Set<StatusDoacao>>> TRANSICOES = Map.of(
            Ator.DOADOR, Map.of(
                    StatusDoacao.PROMETIDA, Set.of(StatusDoacao.ENTREGUE, StatusDoacao.CANCELADA)),
            Ator.CENTRO, Map.of(
                    StatusDoacao.PROMETIDA, Set.of(StatusDoacao.CONFIRMADA, StatusDoacao.CANCELADA),
                    StatusDoacao.ENTREGUE, Set.of(StatusDoacao.CONFIRMADA)));

    private final DoacaoRepository doacoes;
    private final CentroDoacaoRepository centros;
    private final CategoriaDoacaoRepository categorias;
    private final UsuarioRepository usuarios;

    public DoacaoService(DoacaoRepository doacoes, CentroDoacaoRepository centros,
                         CategoriaDoacaoRepository categorias, UsuarioRepository usuarios) {
        this.doacoes = doacoes;
        this.centros = centros;
        this.categorias = categorias;
        this.usuarios = usuarios;
    }

    /** Doador registra a intenção de doar ("PROMETIDA") para um centro aprovado e ativo. */
    @Transactional
    public DoacaoResponse registrar(Long usuarioId, DoacaoRequest req) {
        Usuario doador = usuarios.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        CentroDoacao centro = centros.findById(req.centroId())
                .filter(c -> c.getStatus() == StatusCentro.APROVADO)
                .orElseThrow(() -> new ResourceNotFoundException("Centro não encontrado: " + req.centroId()));
        if (!Boolean.TRUE.equals(centro.getAtivo())) {
            throw new ConflitoException("Este centro não está recebendo doações no momento.");
        }

        String observacao = req.observacao() == null || req.observacao().isBlank() ? null : req.observacao().trim();
        Doacao doacao = new Doacao(doador, centro, observacao);

        for (ItemRequest item : req.itens()) {
            CategoriaDoacao categoria = categorias.findBySlug(item.categoria())
                    .orElseThrow(() -> new RequisicaoInvalidaException("Categoria inexistente: " + item.categoria()));
            doacao.adicionarItem(new ItemDoacao(categoria, item.descricao().trim(), item.quantidade()));
        }

        doacoes.save(doacao);
        return toResponse(doacao, false);
    }

    public List<DoacaoResponse> minhas(Long usuarioId) {
        return doacoes.findByUsuarioIdOrderByCriadoEmDesc(usuarioId).stream()
                .map(d -> toResponse(d, false))
                .toList();
    }

    public List<DoacaoResponse> recebidas(Long responsavelId) {
        return doacoes.findByCentroResponsavelIdOrderByCriadoEmDesc(responsavelId).stream()
                .map(d -> toResponse(d, true))
                .toList();
    }

    /**
     * Muda o status respeitando quem pode fazer o quê.
     * Quem não é o doador nem o responsável pelo centro recebe 404 (não revela que a doação existe).
     */
    @Transactional
    public DoacaoResponse alterarStatus(Long usuarioId, Long doacaoId, StatusDoacao novo) {
        Doacao doacao = doacoes.findById(doacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Doação não encontrada: " + doacaoId));

        boolean ehDoador = doacao.getUsuario().getId().equals(usuarioId);
        Usuario responsavel = doacao.getCentro().getResponsavel();
        boolean ehCentro = responsavel != null && responsavel.getId().equals(usuarioId);

        if (!ehDoador && !ehCentro) {
            throw new ResourceNotFoundException("Doação não encontrada: " + doacaoId);
        }

        StatusDoacao atual = doacao.getStatus();
        boolean permitido = (ehCentro && podeMudar(Ator.CENTRO, atual, novo))
                || (ehDoador && podeMudar(Ator.DOADOR, atual, novo));
        if (!permitido) {
            throw new ConflitoException("Não é possível mudar a doação de " + doacao.getStatus() + " para " + novo + ".");
        }

        doacao.setStatus(novo);
        return toResponse(doacao, ehCentro);
    }

    static boolean podeMudar(Ator ator, StatusDoacao atual, StatusDoacao novo) {
        return TRANSICOES.getOrDefault(ator, Map.of())
                .getOrDefault(atual, Set.of())
                .contains(novo);
    }

    private static DoacaoResponse toResponse(Doacao d, boolean mostrarDoador) {
        List<ItemResponse> itens = d.getItens().stream()
                .map(i -> new ItemResponse(i.getCategoria().getSlug(), i.getCategoria().getNome(),
                        i.getDescricao(), i.getQuantidade()))
                .toList();
        CentroDoacao c = d.getCentro();
        return new DoacaoResponse(
                d.getId(),
                c.getId(),
                c.getNome(),
                c.getEndereco(),
                mostrarDoador ? d.getUsuario().getNome() : null,
                d.getStatus(),
                d.getObservacao(),
                d.getCriadoEm(),
                d.getAtualizadoEm(),
                itens);
    }
}
