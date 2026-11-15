package com.doeja.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "itens_doacao")
public class ItemDoacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doacao_id", nullable = false)
    private Doacao doacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaDoacao categoria;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false)
    private Integer quantidade = 1;

    public ItemDoacao() {
    }

    public ItemDoacao(CategoriaDoacao categoria, String descricao, int quantidade) {
        this.categoria = categoria;
        this.descricao = descricao;
        this.quantidade = quantidade;
    }

    public Long getId() { return id; }
    public Doacao getDoacao() { return doacao; }
    void setDoacao(Doacao doacao) { this.doacao = doacao; }
    public CategoriaDoacao getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public Integer getQuantidade() { return quantidade; }
}
