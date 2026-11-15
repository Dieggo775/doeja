package com.doeja.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.doeja.entity.Doacao;
import com.doeja.entity.StatusDoacao;

@Repository
public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    /** Doações feitas por um doador (privacidade: ele só vê as próprias). */
    List<Doacao> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    /** Doações recebidas pelos centros de um responsável. */
    List<Doacao> findByCentroResponsavelIdOrderByCriadoEmDesc(Long responsavelId);

    long countByStatus(StatusDoacao status);

    /** Total de itens (somando quantidades) das doações confirmadas, por categoria. */
    @Query("""
            select i.categoria.nome, coalesce(sum(i.quantidade), 0) from ItemDoacao i
            where i.doacao.status = com.doeja.entity.StatusDoacao.CONFIRMADA
            group by i.categoria.nome, i.categoria.ordem
            order by i.categoria.ordem
            """)
    List<Object[]> somarItensConfirmadosPorCategoria();

    /** Pares (criada, atualizada) das confirmadas: base do tempo médio até a confirmação. */
    @Query("""
            select d.criadoEm, d.atualizadoEm from Doacao d
            where d.status = com.doeja.entity.StatusDoacao.CONFIRMADA
            """)
    List<Object[]> datasDasConfirmadas();
}
